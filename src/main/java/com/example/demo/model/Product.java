package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    
    
    @Column(columnDefinition = "TEXT")
    private String nameJson; 
    
    @Column(columnDefinition = "TEXT")
    private String descriptionJson; 
    
    private Double price;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();

 
    @JsonProperty("name")
    public String getName() {
        return nameJson;
    }

    public void setName(String name) {
        this.nameJson = name;
    }

    @JsonProperty("description")
    public String getDescription() {
        return descriptionJson;
    }

    public void setDescription(String description) {
        this.descriptionJson = description;
    }

   
    public void setNameMultilingual(Map<String, String> nameMap) {
        this.nameJson = convertMapToJson(nameMap);
    }

    public void setDescriptionMultilingual(Map<String, String> descMap) {
        this.descriptionJson = convertMapToJson(descMap);
    }

    private String convertMapToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) return "{}";
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getNameJson() {
		return nameJson;
	}

	public void setNameJson(String nameJson) {
		this.nameJson = nameJson;
	}

	public String getDescriptionJson() {
		return descriptionJson;
	}

	public void setDescriptionJson(String descriptionJson) {
		this.descriptionJson = descriptionJson;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public List<ProductImage> getImages() {
		return images;
	}

	public void setImages(List<ProductImage> images) {
		this.images = images;
	}
}