package com.example.demo.services;

import com.example.demo.dto.AdminLoginDTO;
import com.example.demo.model.AdminSignupRequest;
import com.example.demo.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminLoginService {

    @Autowired
    private AdminSignupRequestRepository adminSignupRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AdminSignupRequest authenticateAdmin(AdminLoginDTO loginDTO) {
        // Find admin by email
        Optional<AdminSignupRequest> adminOpt = adminSignupRequestRepository.findByEmail(loginDTO.getEmail());
        
        if (adminOpt.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        AdminSignupRequest admin = adminOpt.get();

        // Verify password (removed approval status check - allows login regardless of status)
        if (!passwordEncoder.matches(loginDTO.getPassword(), admin.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return admin;
    }
}