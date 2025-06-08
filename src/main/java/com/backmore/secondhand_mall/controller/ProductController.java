package com.backmore.secondhand_mall.controller;

import com.backmore.secondhand_mall.dto.ProductRequest;
import com.backmore.secondhand_mall.dto.ProductResponse;
import com.backmore.secondhand_mall.entity.Product;
import com.backmore.secondhand_mall.entity.User;
import com.backmore.secondhand_mall.service.ProductService;
import com.backmore.secondhand_mall.service.UserService;
import com.backmore.secondhand_mall.util.JsonUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建商品
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> requestMap) {
        try {
            // 打印认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("创建商品失败: 用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未登录");
            }
            
            logger.info("尝试创建商品，认证用户: {}", authentication.getName());
            
            // 获取当前登录用户ID
            Long userId = getCurrentUserId();
            logger.info("当前用户ID: {}", userId);
            
            // 处理images字段
            Object imagesObj = requestMap.get("images");
            if (imagesObj != null && imagesObj instanceof String) {
                String imagesStr = (String) imagesObj;
                logger.info("收到图片数据作为字符串: {}", imagesStr.substring(0, Math.min(imagesStr.length(), 50)) + "...");
                try {
                    // 尝试解析JSON字符串为List<String>
                    List<String> imagesList = objectMapper.readValue(imagesStr, new TypeReference<List<String>>() {});
                    requestMap.put("images", imagesList);
                    logger.info("成功将图片字符串转换为List<String>, 大小: {}", imagesList.size());
                } catch (Exception e) {
                    logger.error("解析图片JSON字符串失败", e);
                }
            }
            
            // 转换为ProductRequest对象
            ProductRequest request = objectMapper.convertValue(requestMap, ProductRequest.class);
            
            // 设置卖家ID
            if (request.getSellerId() == null) {
                request.setSellerId(userId);
                logger.info("设置卖家ID为当前用户: {}", userId);
            } else if (!request.getSellerId().equals(userId)) {
                // 验证用户只能为自己创建商品
                logger.error("创建商品失败: 卖家ID与当前用户不匹配, 卖家ID: {}, 用户ID: {}", request.getSellerId(), userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("不能为其他用户创建商品");
            }
            
            // 创建商品
            logger.info("开始创建商品: {}, 图片数量: {}", request.getName(), 
                    request.getImages() != null ? request.getImages().size() : 0);
            Product product = productService.createProduct(request);
            
            // 转换为响应对象
            ProductResponse response = ProductResponse.fromEntity(product);
            logger.info("商品创建成功: ID={}", product.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("创建商品时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("创建商品失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            // 获取当前登录用户ID
            Long userId = getCurrentUserId();
            
            // 验证商品所有权
            if (!productService.isProductOwnedByUser(id, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权删除此商品");
            }
            
            productService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除商品失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Optional<Product> productOpt = productService.findById(id);
            
            if (!productOpt.isPresent()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "商品不存在: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Product product = productOpt.get();
            
            // 增加浏览量
            productService.incrementViews(id);
            
            // 增加返回卖家信息
            Map<String, Object> enrichedProduct = new HashMap<>();
            // 复制产品的所有属性到新的map中
            enrichedProduct.put("id", product.getId());
            enrichedProduct.put("name", product.getName());
            enrichedProduct.put("description", product.getDescription());
            enrichedProduct.put("price", product.getPrice());
            enrichedProduct.put("originalPrice", product.getOriginalPrice());
            enrichedProduct.put("categoryId", product.getCategoryId());
            enrichedProduct.put("stock", product.getStock());
            enrichedProduct.put("mainImage", product.getMainImage());
            enrichedProduct.put("images", product.getImages());
            enrichedProduct.put("status", product.getStatus());
            enrichedProduct.put("sellerId", product.getSellerId());
            enrichedProduct.put("brand", product.getBrand());
            enrichedProduct.put("condition", product.getCondition());
            enrichedProduct.put("usedDuration", product.getUsedDuration());
            enrichedProduct.put("purchaseDate", product.getPurchaseDate());
            enrichedProduct.put("faceToFace", product.getFaceToFace());
            enrichedProduct.put("delivery", product.getDelivery());
            enrichedProduct.put("faceToFaceLocation", product.getFaceToFaceLocation());
            enrichedProduct.put("createTime", product.getCreateTime());
            enrichedProduct.put("updateTime", product.getUpdateTime());
            
            // 添加卖家信息
            try {
                Optional<User> sellerOpt = userService.findById(product.getSellerId());
                if (sellerOpt.isPresent()) {
                    User seller = sellerOpt.get();
                    enrichedProduct.put("seller", seller.getUsername());
                    enrichedProduct.put("sellerAvatar", seller.getAvatar());
                } else {
                    enrichedProduct.put("seller", "用户" + product.getSellerId());
                }
            } catch (Exception e) {
                enrichedProduct.put("seller", "用户" + product.getSellerId());
            }
            
            return ResponseEntity.ok(enrichedProduct);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "获取商品详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取所有商品
     */
    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> productPage = productService.findAll(pageable);
            
            // 转换为响应对象
            Page<ProductResponse> responsePage = productPage.map(ProductResponse::fromEntity);
            
            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> requestMap) {
        try {
            // 获取当前登录用户ID
            Long userId = getCurrentUserId();
            
            // 验证商品所有权
            if (!productService.isProductOwnedByUser(id, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权更新此商品");
            }
            
            // 处理images字段
            Object imagesObj = requestMap.get("images");
            if (imagesObj != null && imagesObj instanceof String) {
                String imagesStr = (String) imagesObj;
                logger.info("收到图片数据作为字符串: {}", imagesStr.substring(0, Math.min(imagesStr.length(), 50)) + "...");
                try {
                    // 尝试解析JSON字符串为List<String>
                    List<String> imagesList = objectMapper.readValue(imagesStr, new TypeReference<List<String>>() {});
                    requestMap.put("images", imagesList);
                    logger.info("成功将图片字符串转换为List<String>, 大小: {}", imagesList.size());
                } catch (Exception e) {
                    logger.error("解析图片JSON字符串失败", e);
                }
            }
            
            // 转换为ProductRequest对象
            ProductRequest request = objectMapper.convertValue(requestMap, ProductRequest.class);
            
            // 更新商品
            Product product = productService.updateProduct(id, request);
            
            // 转换为响应对象
            ProductResponse response = ProductResponse.fromEntity(product);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新商品失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新商品失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户商品
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserProducts() {
        try {
            // 获取当前登录用户ID
            Long userId = getCurrentUserId();
            
            // 获取用户商品
            List<Product> products = productService.findBySellerId(userId);
            
            // 转换为响应对象
            List<ProductResponse> responses = products.stream()
                    .map(ProductResponse::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取用户商品失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新商品状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateProductStatus(
            @PathVariable Long id,
            @RequestParam Boolean online) {
        try {
            // 获取当前登录用户ID
            Long userId = getCurrentUserId();
            
            // 验证商品所有权
            if (!productService.isProductOwnedByUser(id, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权更新此商品状态");
            }
            
            // 更新商品状态
            Product product = productService.updateStatus(id, online);
            
            // 转换为响应对象
            ProductResponse response = ProductResponse.fromEntity(product);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新商品状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("获取用户ID失败: 用户未登录");
            throw new RuntimeException("用户未登录");
        }
        
        String username = authentication.getName();
        logger.info("尝试通过用户名获取用户: {}", username);
        Optional<User> userOpt = userService.findByUsername(username);
        if (!userOpt.isPresent()) {
            logger.error("获取用户ID失败: 用户不存在");
            throw new RuntimeException("用户不存在");
        }
        
        logger.info("找到用户: ID={}", userOpt.get().getId());
        return userOpt.get().getId();
    }
}
