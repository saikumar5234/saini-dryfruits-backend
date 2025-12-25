package com.example.demo.dto;

import java.util.List;
import java.util.Map;

public class ProductResponseDTO {

    private Long id;
    private String category;
    private Double price;
    private boolean disabled;

    private Map<String, String> name;
    private Map<String, String> description;

    private List<Long> imageIds;

    // getters & setters

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public Double getPrice() {
        return price;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public Map<String, String> getName() {
        return name;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public List<Long> getImageIds() {
        return imageIds;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public void setImageIds(List<Long> imageIds) {
        this.imageIds = imageIds;
    }
}
