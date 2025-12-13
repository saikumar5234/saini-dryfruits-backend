package com.example.demo.services;

import com.example.demo.model.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Find user by mobile number
     */
    public Optional<User> findByMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByMobile(mobile.trim());
    }
    
    /**
     * Find user by GST number
     */
    public Optional<User> findByGstNumber(String gstNumber) {
        if (gstNumber == null || gstNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByGstNumber(gstNumber.trim().toUpperCase());
    }
    
    /**
     * Save or update user
     */
    public User save(User user) {
        // Normalize GST number to uppercase before saving
        if (user.getGstNumber() != null) {
            user.setGstNumber(user.getGstNumber().trim().toUpperCase());
        }
        
        // Normalize mobile number (trim)
        if (user.getMobile() != null) {
            user.setMobile(user.getMobile().trim());
        }
        
        // Normalize names (trim)
        if (user.getFirstName() != null) {
            user.setFirstName(user.getFirstName().trim());
        }
        if (user.getLastName() != null) {
            user.setLastName(user.getLastName().trim());
        }
        
        return userRepository.save(user);
    }
}
