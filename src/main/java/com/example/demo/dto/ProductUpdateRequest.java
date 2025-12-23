package com.example.demo.dto;

import jakarta.persistence.Column;

public class ProductUpdateRequest {
	 @Column(columnDefinition = "TEXT")
	    private String nameJson; 
	    
	    @Column(columnDefinition = "TEXT")
	    private String descriptionJson; 
	    
	    private String category; 
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
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
	
}