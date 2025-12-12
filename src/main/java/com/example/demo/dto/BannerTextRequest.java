package com.example.demo.dto;

public class BannerTextRequest {
    private String text;
    
    public BannerTextRequest() {
    }
    
    public BannerTextRequest(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}