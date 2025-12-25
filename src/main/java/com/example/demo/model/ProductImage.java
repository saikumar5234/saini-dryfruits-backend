package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_image")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // getters & setters
    public Long getId() {
        return id;
    }

    public byte[] getImage() {
        return image;
    }

    public Product getProduct() {
        return product;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
