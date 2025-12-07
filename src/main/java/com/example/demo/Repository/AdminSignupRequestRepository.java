package com.example.demo.Repository;

import com.example.demo.model.AdminSignupRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminSignupRequestRepository extends JpaRepository<AdminSignupRequest, Long> {
    Optional<AdminSignupRequest> findByEmail(String email);
    Optional<AdminSignupRequest> findByMobile(String mobile);
    
}