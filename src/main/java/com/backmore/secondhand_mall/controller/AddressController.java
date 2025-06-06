package com.backmore.secondhand_mall.controller;

import com.backmore.secondhand_mall.entity.Address;
import com.backmore.secondhand_mall.entity.User;
import com.backmore.secondhand_mall.service.AddressService;
import com.backmore.secondhand_mall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取用户的所有收货地址
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserAddresses(@PathVariable Long userId) {
        try {
            // 验证权限
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // 只有管理员或用户本人可以查看地址
            if (!isAdmin && !user.getUsername().equals(currentUsername)) {
                return ResponseEntity.status(403).body(Map.of("error", "无权查看其他用户的地址"));
            }
            
            List<Address> addresses = addressService.findByUserId(userId);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 获取用户的默认收货地址
     */
    @GetMapping("/user/{userId}/default")
    public ResponseEntity<?> getDefaultAddress(@PathVariable Long userId) {
        try {
            // 验证权限
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // 只有管理员或用户本人可以查看地址
            if (!isAdmin && !user.getUsername().equals(currentUsername)) {
                return ResponseEntity.status(403).body(Map.of("error", "无权查看其他用户的地址"));
            }
            
            Optional<Address> defaultAddress = addressService.findDefaultByUserId(userId);
            return defaultAddress.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 添加收货地址
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> addAddress(@PathVariable Long userId, @RequestBody Address addressRequest) {
        try {
            // 验证权限
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // 只有管理员或用户本人可以添加地址
            if (!isAdmin && !user.getUsername().equals(currentUsername)) {
                return ResponseEntity.status(403).body(Map.of("error", "无权为其他用户添加地址"));
            }
            
            // 设置用户
            addressRequest.setUser(user);
            
            // 保存地址
            Address savedAddress = addressService.save(addressRequest);
            
            return ResponseEntity.ok(savedAddress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 更新收货地址
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(@PathVariable Long addressId, @RequestBody Address addressRequest) {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            
            System.out.println("更新地址请求: ID=" + addressId + ", 当前用户=" + currentUsername);
            System.out.println("请求数据: " + addressRequest);
            
            // 获取地址
            Optional<Address> addressOpt = addressService.findById(addressId);
            if (addressOpt.isEmpty()) {
                System.out.println("地址不存在: ID=" + addressId);
                return ResponseEntity.notFound().build();
            }
            
            Address existingAddress = addressOpt.get();
            Long userId = existingAddress.getUser().getId();
            
            System.out.println("地址所属用户ID=" + userId + ", 用户名=" + existingAddress.getUser().getUsername());
            
            // 验证权限
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            boolean isOwner = existingAddress.getUser().getUsername().equals(currentUsername);
            
            System.out.println("权限检查: isAdmin=" + isAdmin + ", isOwner=" + isOwner);
            
            if (!isAdmin && !isOwner) {
                System.out.println("权限不足，无法修改地址");
                return ResponseEntity.status(403).body(Map.of("error", "无权修改此地址"));
            }
            
            // 设置ID和用户，确保不会被篡改
            addressRequest.setId(addressId);
            addressRequest.setUser(existingAddress.getUser());
            
            // 添加调试日志
            System.out.println("更新地址: ID=" + addressId + ", 用户ID=" + userId);
            System.out.println("收件人=" + addressRequest.getRecipientName() + ", 电话=" + addressRequest.getRecipientPhone());
            
            try {
                // 更新地址
                Address updatedAddress = addressService.update(addressRequest, userId);
                System.out.println("地址更新成功: ID=" + updatedAddress.getId());
                
                return ResponseEntity.ok(updatedAddress);
            } catch (Exception e) {
                System.out.println("地址更新服务层异常: " + e.getMessage());
                e.printStackTrace();
                
                // 即使出错，也尝试返回原地址信息
                return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "message", "地址已保存但返回数据时出错",
                    "address", existingAddress
                ));
            }
        } catch (Exception e) {
            System.out.println("地址更新控制器异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 删除收货地址
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            
            // 获取地址
            Optional<Address> addressOpt = addressService.findById(addressId);
            if (addressOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Address address = addressOpt.get();
            Long userId = address.getUser().getId();
            
            // 验证权限
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            boolean isOwner = address.getUser().getUsername().equals(currentUsername);
            
            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(403).body(Map.of("error", "无权删除此地址"));
            }
            
            // 删除地址
            addressService.delete(addressId, userId);
            
            return ResponseEntity.ok(Map.of("message", "地址删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 设置默认收货地址
     */
    @PutMapping("/{addressId}/default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long addressId) {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            
            // 获取地址
            Optional<Address> addressOpt = addressService.findById(addressId);
            if (addressOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Address address = addressOpt.get();
            Long userId = address.getUser().getId();
            
            // 验证权限
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            boolean isOwner = address.getUser().getUsername().equals(currentUsername);
            
            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(403).body(Map.of("error", "无权修改此地址"));
            }
            
            // 设置默认地址
            Address defaultAddress = addressService.setDefault(addressId, userId);
            
            return ResponseEntity.ok(defaultAddress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 