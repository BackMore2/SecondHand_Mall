package com.backmore.secondhand_mall.vo;

public class ResponseV0<T> {
    private boolean success;
    private String message;
    private T data;

    public ResponseV0(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseV0<T> success(T data) {
        return new ResponseV0<>(true, "操作成功", data);
    }

    public static <T> ResponseV0<T> error(String message) {
        return new ResponseV0<>(false, message, null);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}