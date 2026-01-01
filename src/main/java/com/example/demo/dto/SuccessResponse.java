package com.example.demo.dto;

public class SuccessResponse {
    
    private boolean success;
    private String message;
    
    public SuccessResponse() {
        this.success = true;
    }
    
    public SuccessResponse(String message) {
        this();
        this.message = message;
    }
    
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
}