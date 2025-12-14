package com.example.demo.services;

import com.example.demo.Repository.AdminOtpRepository;
import com.example.demo.Repository.AdminSignupRequestRepository;
import com.example.demo.model.AdminOtp;
import com.example.demo.model.AdminSignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AdminOtpService {

    @Autowired
    private AdminOtpRepository otpRepo;

    @Autowired
    private AdminSignupRequestRepository adminRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SmsService smsService;

    // 🔥 SUPER ADMIN MOBILE
    @Value("${superadmin.mobile}")
    private String superAdminMobile;

    // 🔹 SEND OTP (UPDATED)
    public void sendOtp(String email) {

        AdminSignupRequest admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        // 🔥 FIND / CREATE OTP USING SUPER ADMIN MOBILE
        AdminOtp otpEntity = otpRepo
                .findByEmailAndMobile(email, superAdminMobile)
                .orElse(new AdminOtp());

        otpEntity.setEmail(email);
        otpEntity.setMobile(superAdminMobile); // 🔥 SUPER ADMIN MOBILE
        otpEntity.setOtpHash(passwordEncoder.encode(otp));
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpEntity.setAttempts(0);

        otpRepo.save(otpEntity);

        // 🔥 SEND OTP TO SUPER ADMIN
        smsService.sendOtp(superAdminMobile, otp);

        System.out.println("OTP sent to SUPER ADMIN: " + superAdminMobile);
    }

    // 🔹 VERIFY OTP (NO LOGIC CHANGE)
    public AdminSignupRequest verifyOtp(String email, String mobile, String otp) {

        AdminOtp otpEntity = otpRepo.findByEmailAndMobile(email, mobile)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (otpEntity.getAttempts() >= 3) {
            throw new RuntimeException("Maximum attempts exceeded");
        }

        if (!passwordEncoder.matches(otp, otpEntity.getOtpHash())) {
            otpEntity.setAttempts(otpEntity.getAttempts() + 1);
            otpRepo.save(otpEntity);
            throw new RuntimeException("Invalid OTP");
        }

        otpRepo.delete(otpEntity);

        return adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }
}
