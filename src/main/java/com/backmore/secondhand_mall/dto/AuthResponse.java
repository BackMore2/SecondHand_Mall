package com.backmore.secondhand_mall.dto;

public class AuthResponse {
    private String token;
    private String message;

    public void setToken(String token) {
        this.token = token;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}