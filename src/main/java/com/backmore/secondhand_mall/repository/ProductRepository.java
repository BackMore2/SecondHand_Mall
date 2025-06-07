package com.backmore.secondhand_mall.repository;

import com.backmore.secondhand_mall.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 根据卖家ID查询商品
    List<Product> findBySellerId(Long sellerId);
    
    // 分页查询卖家商品
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);
    
    // 根据状态查询商品
    List<Product> findByStatus(Boolean status);
    
    // 根据分类查询商品
    List<Product> findByCategoryId(Long categoryId);
    
    // 根据分类和状态查询商品
    List<Product> findByCategoryIdAndStatus(Long categoryId, Boolean status);
}
