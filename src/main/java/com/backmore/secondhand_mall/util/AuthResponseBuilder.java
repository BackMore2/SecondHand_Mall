package com.backmore.secondhand_mall.util;

import com.backmore.secondhand_mall.dto.AuthResponse;
import org.springframework.stereotype.Component;

/**
 * 构建 AuthResponse 对象的工具类
 */
@Component
public class AuthResponseBuilder {
    /**
     * 创建并返回 AuthResponse 对象
     * @param token 认证 token
     * @param message 响应消息
     * @return AuthResponse 对象
     */
    public AuthResponse buildAuthResponse(String token, String message) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setMessage(message);
        return response;
    }
}