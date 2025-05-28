
package com.backmore.secondhand_mall.service;

import com.backmore.secondhand_mall.entity.Cart;
import com.backmore.secondhand_mall.entity.CartItem;
import java.util.List;

public interface CartService {
    Cart getCartByUserId(Long userId);
    Cart createCart(Cart cart);
    Cart addProductToCart(Long userId, Long productId, Integer quantity);
    Cart updateCartItemQuantity(Long cartItemId, Integer quantity);
    void removeProductFromCart(Long cartItemId);
    void clearCart(Long userId);
}