package com.example.demo.dto;

import java.util.Map;

public class CreateProductRequest {

    private String category;
    private Double price;

    // multilingual JSON
    private Map<String, String> name;
    private Map<String, String> description;

    // getters & setters
    public String getCategory() {
        return category;
    }

    public Double getPrice() {
        return price;
    }

    public Map<String, String> getName() {
        return name;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }
}
