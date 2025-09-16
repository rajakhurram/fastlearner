package com.vinncorp.fast_learner.util;

public class Message<T> {
    private int status;
    private String message;
    private String code;

    private T data;

    public Message() {
    }

    public Message(int status, String message, String code, T data) {
        this.status = status;
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public Message<T> setStatus(int status) {
        this.status = status;
        return this;
    }

    public Message<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public Message<T> setCode(String code) {
        this.code = code;
        return this;
    }

    public Message<T> setData(T data) {
        this.data = data;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String  getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

}
