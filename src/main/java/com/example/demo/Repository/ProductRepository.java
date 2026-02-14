package com.example.demo.Repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    long countByCategory(String category);

    @Modifying
    @Query("UPDATE Product p SET p.sortOrder = :sortOrder WHERE p.id = :id")
    void updateSortOrder(@Param("id") Long id, @Param("sortOrder") int sortOrder);

    List<Product> findAllByOrderBySortOrderAscIdAsc();
}