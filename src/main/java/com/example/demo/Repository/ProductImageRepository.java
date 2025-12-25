package com.example.demo.Repository;

import com.example.demo.model.ProductImage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
	@Query("select pi.id from ProductImage pi where pi.product.id = :productId")
    List<Long> findImageIdsByProductId(@Param("productId") Long productId);
}