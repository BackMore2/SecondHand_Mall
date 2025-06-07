package com.backmore.secondhand_mall.repository;

import com.backmore.secondhand_mall.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    void deleteByCartId(Long id);

    CartItem findByCartIdAndProductId(Long id, Long productId);
    
    List<CartItem> findByCartId(Long cartId);
}