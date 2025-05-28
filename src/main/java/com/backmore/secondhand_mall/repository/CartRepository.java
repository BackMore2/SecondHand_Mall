
package com.backmore.secondhand_mall.repository;

import com.backmore.secondhand_mall.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUserId(Long userId);
}