package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class BannerTextRequest {
    // Accept multilingual text as Map (e.g., {"en": "...", "hi": "...", "te": "..."})
    private Map<String, String> text;
    
    // For backward compatibility or direct JSON string input
    @JsonProperty("textJson")
    private String textJson;
    
    private Boolean isActive;
    
    public BannerTextRequest() {
    }
    
    public BannerTextRequest(Map<String, String> text) {
        this.text = text;
    }
    
    public Map<String, String> getText() {
        return text;
    }
    
    public void setText(Map<String, String> text) {
        this.text = text;
    }
    
    public String getTextJson() {
        return textJson;
    }
    
    public void setTextJson(String textJson) {
        this.textJson = textJson;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // Helper method to convert Map to JSON string
    public String getTextJsonString() {
        if (textJson != null && !textJson.isEmpty()) {
            return textJson; // Already a JSON string
        }
        if (text != null && !text.isEmpty()) {
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(text);
            } catch (Exception e) {
                return "{}";
            }
        }
        return "{}";
    }
}