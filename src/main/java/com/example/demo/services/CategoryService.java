package com.example.demo.services;

import com.example.demo.model.Category;
import com.sainidryfruits.exception.CategoryAlreadyExistsException;
import com.sainidryfruits.exception.CategoryInUseException;
import com.sainidryfruits.exception.CategoryNotFoundException;
import com.example.demo.Repository.CategoryRepository;
import com.example.demo.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Create a new category
     * @param name The category name
     * @return The created category
     * @throws IllegalArgumentException if name is null or empty
     * @throws CategoryAlreadyExistsException if category with same name already exists
     */
    public Category createCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        
        String trimmedName = name.trim();
        
        // Check if category already exists (case-insensitive)
        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new CategoryAlreadyExistsException(
                "Category with name '" + trimmedName + "' already exists"
            );
        }
        
        Category category = new Category();
        category.setName(trimmedName);
        return categoryRepository.save(category);
    }
    
    /**
     * Get all categories
     * @return List of all categories
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    /**
     * Get category by ID
     * @param id The category ID
     * @return The category
     * @throws CategoryNotFoundException if category not found
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new CategoryNotFoundException(
                "Category with ID " + id + " not found"
            ));
    }
    
    /**
     * Update a category
     * @param id The category ID
     * @param name The new category name
     * @return The updated category
     * @throws CategoryNotFoundException if category not found
     * @throws IllegalArgumentException if name is null or empty
     * @throws CategoryAlreadyExistsException if another category with same name exists
     */
    public Category updateCategory(Long id, String name) {
        Category category = getCategoryById(id);
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        
        String trimmedName = name.trim();
        
        // Check if another category with the same name exists (case-insensitive)
        Optional<Category> existingCategory = categoryRepository.findByNameIgnoreCase(trimmedName);
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            throw new CategoryAlreadyExistsException(
                "Category with name '" + trimmedName + "' already exists"
            );
        }
        
        category.setName(trimmedName);
        return categoryRepository.save(category);
    }
    
    /**
     * Delete a category
     * @param id The category ID
     * @throws CategoryNotFoundException if category not found
     * @throws CategoryInUseException if category is used by products
     */
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        
        // Check if category is used by any products
        // Assuming Product entity has a category field (String or Category)
        // Adjust this query based on your Product entity structure
        long productCount = productRepository.countByCategory(category.getName());
        
        if (productCount > 0) {
            throw new CategoryInUseException(
                "Cannot delete category. It is used by " + productCount + " product(s)"
            );
        }
        
        categoryRepository.delete(category);
    }
    
    /**
     * Check if a category exists by name
     * @param name The category name
     * @return true if exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean categoryExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return categoryRepository.existsByNameIgnoreCase(name.trim());
    }
}