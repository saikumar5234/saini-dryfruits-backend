package com.example.demo.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;

    @Column(name = "is_disable", nullable = false)
    private boolean isDisabled = false;

    @Column(name = "name_json", columnDefinition = "TEXT")
    private String nameJson;

    @Column(name = "description_json", columnDefinition = "TEXT")
    private String descriptionJson;

    private Double price;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    // getters & setters
    public Long getId() { return id; }
    public String getCategory() { return category; }
    public boolean isDisabled() { return isDisabled; }
    public String getNameJson() { return nameJson; }
    public String getDescriptionJson() { return descriptionJson; }
    public Double getPrice() { return price; }
    public Integer getSortOrder() { return sortOrder; }

    public void setCategory(String category) { this.category = category; }
    public void setDisabled(boolean disabled) { isDisabled = disabled; }
    public void setNameJson(String nameJson) { this.nameJson = nameJson; }
    public void setDescriptionJson(String descriptionJson) { this.descriptionJson = descriptionJson; }
    public void setPrice(Double price) { this.price = price; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
