package com.example.demo.dto;

public class BannerTextResponse {
    private boolean success;
    private String text;
    private String message;
    
    public BannerTextResponse() {
    }
    
    public BannerTextResponse(boolean success, String text, String message) {
        this.success = success;
        this.text = text;
        this.message = message;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
