package com.example.demo.Repository;

import com.example.demo.model.SuperAdminPasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuperAdminPasswordResetOtpRepository
        extends JpaRepository<SuperAdminPasswordResetOtp, Long> {
    Optional<SuperAdminPasswordResetOtp> findBySuperAdminId(Long superAdminId);
}
