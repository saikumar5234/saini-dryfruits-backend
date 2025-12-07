package com.example.demo.services;

import com.example.demo.dto.AdminSignupRequestDTO;
import com.example.demo.model.AdminSignupRequest;
import com.example.demo.model.Status;
import com.example.demo.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminSignupRequestService {

    @Autowired
    private AdminSignupRequestRepository adminSignupRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AdminSignupRequest createAdminSignupRequest(AdminSignupRequestDTO dto) {
        // Check if email already exists
        if (adminSignupRequestRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Check if mobile already exists
        if (adminSignupRequestRepository.findByMobile(dto.getMobile()).isPresent()) {
            throw new RuntimeException("Mobile number already exists");
        }

        // Create new admin signup request
        AdminSignupRequest request = new AdminSignupRequest();
        request.setFirstName(dto.getFirstName());
        request.setLastName(dto.getLastName());
        request.setEmail(dto.getEmail());
        request.setMobile(dto.getMobile());
        
        // Hash the password
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        request.setPasswordHash(hashedPassword);
        
        // Set role (default to "ADMIN" if not provided)
        request.setRole(dto.getRole() != null && !dto.getRole().isEmpty() ? dto.getRole() : "ADMIN");
        
        // Set status to PENDING
        request.setStatus(Status.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        return adminSignupRequestRepository.save(request);
    }
}