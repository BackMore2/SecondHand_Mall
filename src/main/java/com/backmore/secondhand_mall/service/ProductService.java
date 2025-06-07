package com.backmore.secondhand_mall.service;

import com.backmore.secondhand_mall.dto.ProductRequest;
import com.backmore.secondhand_mall.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    // 保存商品
    Product save(Product product);
    
    // 根据请求对象创建商品
    Product createProduct(ProductRequest request);
    
    // 根据请求对象更新商品
    Product updateProduct(Long id, ProductRequest request);
    
    // 删除商品
    void deleteById(Long id);
    
    // 根据ID查找商品
    Optional<Product> findById(Long id);
    
    // 分页查询所有商品
    Page<Product> findAll(Pageable pageable);
    
    // 更新商品
    Product update(Product product);
    
    // 根据卖家ID查询商品
    List<Product> findBySellerId(Long sellerId);
    
    // 分页查询卖家商品
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);
    
    // 更新商品状态（上架/下架）
    Product updateStatus(Long id, Boolean status);
    
    // 增加商品浏览量
    void incrementViews(Long id);
    
    // 增加商品销量
    void incrementSales(Long id, Integer quantity);
    
    // 检查商品是否属于指定卖家
    boolean isProductOwnedByUser(Long productId, Long userId);
}
