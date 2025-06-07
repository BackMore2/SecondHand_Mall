package com.backmore.secondhand_mall.service.impl;

import com.backmore.secondhand_mall.entity.Cart;
import com.backmore.secondhand_mall.entity.CartItem;
import com.backmore.secondhand_mall.entity.Product;
import com.backmore.secondhand_mall.repository.CartItemRepository;
import com.backmore.secondhand_mall.repository.CartRepository;
import com.backmore.secondhand_mall.repository.ProductRepository;
import com.backmore.secondhand_mall.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Cart getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            return null;
        }
        
        // 获取购物车项
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        
        // 获取所有相关的商品信息
        Map<Long, Product> productMap = new HashMap<>();
        for (CartItem item : cartItems) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                productMap.put(product.getId(), product);
            }
        }
        
        // 为CartItem添加商品信息的传输属性
        for (CartItem item : cartItems) {
            Product product = productMap.get(item.getProductId());
            if (product != null) {
                item.setProductName(product.getName());
                item.setProductPrice(product.getPrice());
                item.setProductImage(product.getMainImage());
                item.setProductStock(product.getStock());
            }
        }
        
        // 设置购物车项
        cart.setItems(cartItems);
        
        return cart;
    }

    @Override
    public Cart createCart(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart addProductToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cart = cartRepository.save(cart);
        }

        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setUpdateTime(LocalDateTime.now());
            cartItemRepository.save(existingItem);
        } else {
            CartItem item = new CartItem();
            item.setCartId(cart.getId());
            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            cartItemRepository.save(item);
        }

        // 返回完整的购物车信息
        return getCartByUserId(userId);
    }

    @Override
    @Transactional
    public Cart updateCartItemQuantity(Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId).orElse(null);
        if (item != null) {
            item.setQuantity(quantity);
            item.setUpdateTime(LocalDateTime.now());
            cartItemRepository.save(item);
            
            // 查找对应的购物车
            Cart cart = cartRepository.findById(item.getCartId()).orElse(null);
            if (cart != null) {
                // 返回完整的购物车信息
                return getCartByUserId(cart.getUserId());
            }
        }
        return null;
    }

    @Override
    @Transactional
    public void removeProductFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        try {
            System.out.println("开始清空用户ID为 " + userId + " 的购物车");
            
            Cart cart = cartRepository.findByUserId(userId);
            if (cart != null) {
                Long cartId = cart.getId();
                System.out.println("找到购物车ID: " + cartId);
                
                // 先获取购物车项，记录要删除的数量
                List<CartItem> items = cartItemRepository.findByCartId(cartId);
                System.out.println("购物车中共有 " + items.size() + " 件商品");
                
                // 使用批量删除而不是自定义方法
                if (!items.isEmpty()) {
                    for (CartItem item : items) {
                        System.out.println("删除购物车项: " + item.getId());
                        cartItemRepository.deleteById(item.getId());
                    }
                    System.out.println("成功删除所有购物车项");
                } else {
                    System.out.println("购物车为空，无需删除");
                }
            } else {
                System.out.println("未找到用户ID为 " + userId + " 的购物车");
            }
        } catch (Exception e) {
            System.err.println("清空购物车时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常，确保事务回滚
        }
    }
}