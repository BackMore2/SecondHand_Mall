package com.backmore.secondhand_mall.controller;

import com.backmore.secondhand_mall.entity.Cart;
import com.backmore.secondhand_mall.entity.CartItem;
import com.backmore.secondhand_mall.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public Cart getCartByUserId(@PathVariable Long userId) {
        return cartService.getCartByUserId(userId);
    }

    @PostMapping
    public Cart createCart(@RequestBody Cart cart) {
        return cartService.createCart(cart);
    }

    @PostMapping("/add")
    public Cart addProductToCart(@RequestBody Map<String, Object> requestBody) {
        Long userId = Long.valueOf(requestBody.get("userId").toString());
        Long productId = Long.valueOf(requestBody.get("productId").toString());
        Integer quantity = Integer.valueOf(requestBody.get("quantity").toString());
        return cartService.addProductToCart(userId, productId, quantity);
    }

    @PutMapping("/item/{cartItemId}")
    public Cart updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Object> requestBody) {
        Integer quantity = Integer.valueOf(requestBody.get("quantity").toString());
        return cartService.updateCartItemQuantity(cartItemId, quantity);
    }

    @DeleteMapping("/item/{cartItemId}")
    public void removeProductFromCart(@PathVariable Long cartItemId) {
        cartService.removeProductFromCart(cartItemId);
    }

    @DeleteMapping("/clear/{userId}")
    public Map<String, Object> clearCart(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            cartService.clearCart(userId);
            response.put("success", true);
            response.put("message", "购物车已成功清空");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清空购物车失败: " + e.getMessage());
        }
        return response;
    }
}