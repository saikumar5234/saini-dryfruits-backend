package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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

    // getters & setters
    public Long getId() { return id; }
    public String getCategory() { return category; }
    public boolean isDisabled() { return isDisabled; }
    public String getNameJson() { return nameJson; }
    public String getDescriptionJson() { return descriptionJson; }
    public Double getPrice() { return price; }

    public void setCategory(String category) { this.category = category; }
    public void setDisabled(boolean disabled) { isDisabled = disabled; }
    public void setNameJson(String nameJson) { this.nameJson = nameJson; }
    public void setDescriptionJson(String descriptionJson) { this.descriptionJson = descriptionJson; }
    public void setPrice(Double price) { this.price = price; }
}
