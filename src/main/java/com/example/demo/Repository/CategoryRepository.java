package com.example.demo.Repository;

import com.example.demo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find a category by its name (case-sensitive)
     * @param name The category name
     * @return Optional containing the category if found
     */
    Optional<Category> findByName(String name);
    
    /**
     * Check if a category with the given name exists
     * @param name The category name
     * @return true if category exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Find a category by name ignoring case
     * @param name The category name
     * @return Optional containing the category if found
     */
    Optional<Category> findByNameIgnoreCase(String name);
    
    /**
     * Check if a category with the given name exists (case-insensitive)
     * @param name The category name
     * @return true if category exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);
}
