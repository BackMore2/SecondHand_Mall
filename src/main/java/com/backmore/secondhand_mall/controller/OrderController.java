package com.backmore.secondhand_mall.controller;

import com.backmore.secondhand_mall.entity.Order;
import com.backmore.secondhand_mall.entity.OrderItem;
import com.backmore.secondhand_mall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            // 获取当前认证用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("创建订单失败：用户未认证");
                Map<String, String> response = new HashMap<>();
                response.put("error", "未授权操作，请先登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            logger.info("开始创建订单: {}", order);
            Order createdOrder = orderService.createOrder(order);
            logger.info("订单创建成功: {}", createdOrder.getId());
            return ResponseEntity.ok(createdOrder);
        } catch (Exception e) {
            logger.error("创建订单时发生错误: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "创建订单失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long id,
            @RequestParam(value = "includeItems", defaultValue = "true") boolean includeItems) {
        try {
            logger.info("获取订单详情，ID={}, includeItems={}", id, includeItems);
            Order order = orderService.getOrderByIdWithItems(id, includeItems);
            
            if (order == null) {
                logger.warn("订单不存在: {}", id);
                Map<String, String> response = new HashMap<>();
                response.put("error", "订单不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.error("获取订单详情时发生错误: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取订单失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{id}/items")
    public ResponseEntity<?> getOrderItems(@PathVariable Long id) {
        try {
            logger.info("获取订单项列表，订单ID={}", id);
            
            // 首先检查订单是否存在
            Order order = orderService.getOrderById(id);
            if (order == null) {
                logger.warn("订单不存在: {}", id);
                Map<String, String> response = new HashMap<>();
                response.put("error", "订单不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            List<OrderItem> items = orderService.getOrderItems(id);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            logger.error("获取订单项列表时发生错误: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取订单项失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(value = "includeItems", defaultValue = "false") boolean includeItems) {
        try {
            logger.info("获取用户订单列表，用户ID={}, includeItems={}", userId, includeItems);
            List<Order> orders = orderService.getOrdersByUserIdWithItems(userId, includeItems);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("获取用户订单列表时发生错误: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取订单列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping
    public Order updateOrder(@RequestBody Order order) {
        return orderService.updateOrder(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            logger.info("删除订单，ID={}", id);
            
            // 首先检查订单是否存在
            Order order = orderService.getOrderById(id);
            if (order == null) {
                logger.warn("要删除的订单不存在: {}", id);
                Map<String, String> response = new HashMap<>();
                response.put("error", "订单不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // 删除订单及其订单项
            orderService.deleteOrder(id);
            logger.info("订单已删除: {}", id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "订单删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("删除订单时发生错误: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "删除订单失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            logger.info("取消订单，ID={}", id);
            
            // 首先检查订单是否存在
            Order order = orderService.getOrderById(id);
            if (order == null) {
                logger.warn("订单不存在: {}", id);
                Map<String, String> response = new HashMap<>();
                response.put("error", "订单不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // 检查订单状态是否允许取消
            if (!"PENDING".equals(order.getStatus()) && !"待付款".equals(order.getStatus())) {
                logger.warn("订单状态不允许取消: {} - {}", id, order.getStatus());
                Map<String, String> response = new HashMap<>();
                response.put("error", "只有待付款订单可以取消");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 取消订单
            order.setStatus("CANCELED");
            Order updatedOrder = orderService.updateOrder(order);
            logger.info("订单已取消: {}", id);
            
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            logger.error("取消订单时发生错误: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "取消订单失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 支付订单并直接将状态更新为已完成
     * @param id 订单ID
     * @param paymentData 支付信息
     * @return 更新后的订单
     */
    @PutMapping("/{id}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> paymentData) {
        try {
            logger.info("支付订单，ID={}，支付数据={}", id, paymentData);
            
            // 首先检查订单是否存在
            Order order = orderService.getOrderById(id);
            if (order == null) {
                logger.warn("订单不存在: {}", id);
                Map<String, String> response = new HashMap<>();
                response.put("error", "订单不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // 检查订单状态是否允许支付
            if (!"PENDING".equals(order.getStatus()) && !"待付款".equals(order.getStatus())) {
                logger.warn("订单状态不允许支付: {} - {}", id, order.getStatus());
                Map<String, String> response = new HashMap<>();
                response.put("error", "只有待付款订单可以支付");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 如果提供了支付方式，则更新订单的支付方式
            if (paymentData != null && paymentData.containsKey("paymentMethod")) {
                order.setPaymentMethod((String) paymentData.get("paymentMethod"));
            }
            
            // 直接将订单状态设置为已完成，跳过待发货和已发货状态
            order.setStatus("COMPLETED");
            
            // 更新订单的更新时间
            order.setUpdatedAt(LocalDateTime.now());
            
            // 保存订单
            Order updatedOrder = orderService.updateOrder(order);
            logger.info("订单已支付并完成: {}", id);
            
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            logger.error("支付订单时发生错误: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "支付订单失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 确认收货，将订单状态更新为已完成
     * @param id 订单ID
     * @return 更新后的订单
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id) {
        try {
            logger.info("确认收货，ID={}", id);
            
            // 首先检查订单是否存在
            Order order = orderService.getOrderById(id);
            if (order == null) {
                logger.warn("订单不存在: {}", id);
                Map<String, String> response = new HashMap<>();
                response.put("error", "订单不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // 检查订单状态是否允许确认收货（通常是已发货状态，但我们也允许待发货状态直接确认完成）
            if (!"SHIPPED".equals(order.getStatus()) && !"已发货".equals(order.getStatus()) &&
                !"PAID".equals(order.getStatus()) && !"待发货".equals(order.getStatus())) {
                logger.warn("订单状态不允许确认收货: {} - {}", id, order.getStatus());
                Map<String, String> response = new HashMap<>();
                response.put("error", "只有已发货或待发货订单可以确认收货");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 将订单状态设置为已完成
            order.setStatus("COMPLETED");
            
            // 更新订单的更新时间
            order.setUpdatedAt(LocalDateTime.now());
            
            // 保存订单
            Order updatedOrder = orderService.updateOrder(order);
            logger.info("订单已确认收货并完成: {}", id);
            
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            logger.error("确认收货时发生错误: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "确认收货失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}