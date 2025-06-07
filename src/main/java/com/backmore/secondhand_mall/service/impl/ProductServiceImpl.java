package com.backmore.secondhand_mall.service.impl;

import com.backmore.secondhand_mall.dto.ProductRequest;
import com.backmore.secondhand_mall.entity.Product;
import com.backmore.secondhand_mall.repository.ProductRepository;
import com.backmore.secondhand_mall.service.ProductService;
import com.backmore.secondhand_mall.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }
    
    @Override
    @Transactional
    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        
        // 设置基本信息
        product.setName(request.getName());
        product.setSellerId(request.getSellerId());
        product.setCategoryId(request.getCategoryId());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setCondition(request.getCondition());
        product.setUsedDuration(request.getUsedDuration());
        product.setBrand(request.getBrand());
        product.setPurchaseDate(request.getPurchaseDate());
        product.setFaceToFace(request.getFaceToFace());
        product.setDelivery(request.getDelivery());
        product.setFaceToFaceLocation(request.getFaceToFaceLocation());
        
        // 设置图片 - 支持Base64格式
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // 将图片列表转换为JSON字符串
            product.setImages(JsonUtil.imagesToJson(request.getImages()));
            
            // 设置主图为第一张图片
            product.setMainImage(request.getImages().get(0));
        }
        
        // 设置初始值
        product.setViews(0);
        product.setSales(0);
        product.setStatus(true); // 默认上架
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        
        // 保存商品
        return productRepository.save(product);
    }
    
    @Override
    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        // 查找商品
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (!optionalProduct.isPresent()) {
            throw new RuntimeException("商品不存在");
        }
        
        Product product = optionalProduct.get();
        
        // 更新基本信息
        product.setName(request.getName());
        product.setCategoryId(request.getCategoryId());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setCondition(request.getCondition());
        product.setUsedDuration(request.getUsedDuration());
        product.setBrand(request.getBrand());
        product.setPurchaseDate(request.getPurchaseDate());
        product.setFaceToFace(request.getFaceToFace());
        product.setDelivery(request.getDelivery());
        product.setFaceToFaceLocation(request.getFaceToFaceLocation());
        
        // 更新图片 - 支持Base64格式
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // 将图片列表转换为JSON字符串
            product.setImages(JsonUtil.imagesToJson(request.getImages()));
            
            // 更新主图为第一张图片
            product.setMainImage(request.getImages().get(0));
        }
        
        // 更新时间
        product.setUpdateTime(new Date());
        
        // 保存商品
        return productRepository.save(product);
    }

    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product update(Product product) {
        product.setUpdateTime(new Date());
        return productRepository.save(product);
    }
    
    @Override
    public List<Product> findBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
    
    @Override
    public Page<Product> findBySellerId(Long sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable);
    }
    
    @Override
    @Transactional
    public Product updateStatus(Long id, Boolean status) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (!optionalProduct.isPresent()) {
            throw new RuntimeException("商品不存在");
        }
        
        Product product = optionalProduct.get();
        product.setStatus(status);
        product.setUpdateTime(new Date());
        
        return productRepository.save(product);
    }
    
    @Override
    @Transactional
    public void incrementViews(Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setViews(product.getViews() + 1);
            productRepository.save(product);
        }
    }
    
    @Override
    @Transactional
    public void incrementSales(Long id, Integer quantity) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setSales(product.getSales() + quantity);
            
            // 减少库存
            int newStock = product.getStock() - quantity;
            if (newStock < 0) {
                throw new RuntimeException("商品库存不足");
            }
            product.setStock(newStock);
            
            productRepository.save(product);
        }
    }
    
    @Override
    public boolean isProductOwnedByUser(Long productId, Long userId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        return optionalProduct.isPresent() && optionalProduct.get().getSellerId().equals(userId);
    }
}
