package com.backmore.secondhand_mall.repository;

import com.backmore.secondhand_mall.entity.OrderItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends CrudRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    void deleteByOrderId(Long orderId);
    
}