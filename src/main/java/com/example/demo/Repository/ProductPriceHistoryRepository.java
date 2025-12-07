
package com.example.demo.Repository;

import com.example.demo.model.ProductPriceHistory;
import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductPriceHistoryRepository extends JpaRepository<ProductPriceHistory, Long> {
    void deleteByProduct(Product product);
    List<ProductPriceHistory> findByProductOrderByChangedAtAsc(Product product);
}
