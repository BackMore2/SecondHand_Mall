package com.backmore.secondhand_mall.service.impl;

import com.backmore.secondhand_mall.entity.Order;
import com.backmore.secondhand_mall.entity.OrderItem;
import com.backmore.secondhand_mall.repository.OrderItemRepository;
import com.backmore.secondhand_mall.repository.OrderRepository;
import com.backmore.secondhand_mall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // 生成唯一订单号
        String orderNumber = generateOrderNumber();
        order.setOrderNumber(orderNumber);
        
        // 保存订单项列表（暂时）
        List<OrderItem> items = order.getItems();
        
        // 先清空订单项以便保存订单本身
        order.setItems(null);
        
        // 设置订单创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        
        // 设置初始状态
        if (order.getStatus() == null) {
            order.setStatus("PENDING");
        }
        
        logger.info("正在创建订单，订单号: {}", orderNumber);
        
        try {
            // 先保存订单以获取订单ID
            Order savedOrder = orderRepository.save(order);
            logger.info("订单基本信息已保存，ID: {}", savedOrder.getId());
            
            // 如果有订单项，处理并保存
            if (items != null && !items.isEmpty()) {
                List<OrderItem> savedItems = new ArrayList<>();
                
                for (OrderItem item : items) {
                    // 设置订单ID
                    item.setOrderId(savedOrder.getId());
                    
                    // 设置创建时间和更新时间
                    item.setCreatedAt(now);
                    item.setUpdatedAt(now);
                    
                    // 计算总价
                    if (item.getTotalPrice() == null && item.getPrice() != null && item.getQuantity() != null) {
                        BigDecimal totalPrice = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        item.setTotalPrice(totalPrice);
                    }
                    
                    // 保存订单项
                    OrderItem savedItem = orderItemRepository.save(item);
                    savedItems.add(savedItem);
                    
                    logger.info("已保存订单项: 产品ID={}, 数量={}", item.getProductId(), item.getQuantity());
                }
                
                // 设置保存后的订单项列表
                savedOrder.setItems(savedItems);
            }
            
            logger.info("订单创建成功: {}", savedOrder.getId());
            return savedOrder;
        } catch (Exception e) {
            logger.error("保存订单失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 生成唯一的订单号
     * 格式：年月日时分秒 + 4位随机数
     */
    private String generateOrderNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String dateTime = LocalDateTime.now().format(formatter);
        
        // 生成4位随机数
        Random random = new Random();
        int randomNumber = random.nextInt(10000);
        String randomStr = String.format("%04d", randomNumber);
        
        return dateTime + randomStr;
    }

    @Override
    public Order getOrderById(Long id) {
        return getOrderByIdWithItems(id, true);
    }
    
    @Override
    public Order getOrderByIdWithItems(Long id, boolean includeItems) {
        logger.info("获取订单ID={}, 是否包含订单项={}", id, includeItems);
        Order order = orderRepository.findById(id).orElse(null);
        
        if (order != null && !includeItems) {
            // 如果不需要包含订单项，则设置为null
            order.setItems(null);
            logger.info("获取订单（不含订单项）成功: {}", order.getId());
        } else if (order != null) {
            logger.info("获取订单（含订单项）成功: {}", order.getId());
        }
        
        return order;
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return getOrdersByUserIdWithItems(userId, true);
    }
    
    @Override
    public List<Order> getOrdersByUserIdWithItems(Long userId, boolean includeItems) {
        logger.info("获取用户ID={}的订单列表, 是否包含订单项={}", userId, includeItems);
        List<Order> orders = orderRepository.findByUserId(userId);
        
        if (!includeItems) {
            // 如果不需要包含订单项，则对每个订单设置items为null
            for (Order order : orders) {
                order.setItems(null);
            }
            logger.info("获取用户订单列表（不含订单项）成功, 共{}条记录", orders.size());
        } else {
            logger.info("获取用户订单列表（含订单项）成功, 共{}条记录", orders.size());
        }
        
        return orders;
    }
    
    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        logger.info("获取订单ID={}的订单项列表", orderId);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        logger.info("获取订单项列表成功, 共{}条记录", items.size());
        return items;
    }

    @Override
    public List<Order> getAllOrders() {
        return (List<Order>) orderRepository.findAll();
    }

    @Override
    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        logger.info("删除订单及其订单项，订单ID={}", id);
        try {
            // 先删除关联的订单项
            orderItemRepository.deleteByOrderId(id);
            logger.info("订单项已删除，订单ID={}", id);
            
            // 再删除订单
            orderRepository.deleteById(id);
            logger.info("订单已删除，订单ID={}", id);
        } catch (Exception e) {
            logger.error("删除订单失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}