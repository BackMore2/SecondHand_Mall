package com.backmore.secondhand_mall.repository;

import com.backmore.secondhand_mall.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 可根据需要添加自定义查询方法
}
