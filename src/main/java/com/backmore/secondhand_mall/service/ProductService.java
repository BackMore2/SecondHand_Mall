package com.backmore.secondhand_mall.service;

import com.backmore.secondhand_mall.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ProductService {
    Product save(Product product);
    void deleteById(Long id);
    Optional<Product> findById(Long id);
    Page<Product> findAll(Pageable pageable);
    Product update(Product product);
}
