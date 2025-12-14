package com.example.demo.Repository;

import com.example.demo.model.AdminOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminOtpRepository extends JpaRepository<AdminOtp, Long> {

    Optional<AdminOtp> findByEmailAndMobile(String email, String mobile);
}
