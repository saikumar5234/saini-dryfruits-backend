package com.example.demo.dto;

import java.util.Map;

public class BannerTextResponse {
    private boolean success;
    private String message;
    private Map<String, String> text; // Multilingual text
    private Boolean isActive;
    
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
    
    public Map<String, String> getText() {
        return text;
    }
    
    public void setText(Map<String, String> text) {
        this.text = text;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}