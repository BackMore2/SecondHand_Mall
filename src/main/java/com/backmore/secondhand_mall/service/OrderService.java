
package com.backmore.secondhand_mall.service;

import com.backmore.secondhand_mall.entity.Order;
import com.backmore.secondhand_mall.entity.OrderItem;
import java.util.List;

public interface OrderService {
    Order createOrder(Order order);
    Order getOrderById(Long id);
    Order getOrderByIdWithItems(Long id, boolean includeItems);
    List<Order> getOrdersByUserId(Long userId);
    List<Order> getOrdersByUserIdWithItems(Long userId, boolean includeItems);
    List<OrderItem> getOrderItems(Long orderId);
    List<Order> getAllOrders();
    Order updateOrder(Order order);
    void deleteOrder(Long id);
}