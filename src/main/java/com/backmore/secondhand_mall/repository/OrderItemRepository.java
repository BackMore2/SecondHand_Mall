
package com.backmore.secondhand_mall.repository;

import com.backmore.secondhand_mall.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}