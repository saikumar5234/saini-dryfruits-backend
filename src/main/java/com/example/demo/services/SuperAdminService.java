
package com.example.demo.services;

import com.example.demo.Repository.SuperAdminPasswordResetOtpRepository;
import com.example.demo.Repository.SuperAdminRepository;
import com.example.demo.config.JwtUtil;
import com.example.demo.model.SuperAdmin;
import com.example.demo.model.SuperAdminPasswordResetOtp;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

@Service
public class SuperAdminService {

    private static final int MAX_OTP_ATTEMPTS = 5;

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final SuperAdminRepository superAdminRepository;

    private final SuperAdminPasswordResetOtpRepository otpRepository;

    private final PasswordEncoder passwordEncoder;

    private final SmsService smsService;

    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper;

    public SuperAdminService(
            SuperAdminRepository superAdminRepository,
            SuperAdminPasswordResetOtpRepository otpRepository,
            PasswordEncoder passwordEncoder,
            SmsService smsService,
            JwtUtil jwtUtil,
            ObjectMapper objectMapper) {

        this.superAdminRepository = superAdminRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.smsService = smsService;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    // ================= LOGIN =================

    public Map<String, Object> login(
            String username,
            String password) {

        SuperAdmin admin = superAdminRepository
                .findByUsername(username.trim())
                .filter(SuperAdmin::isActive)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Invalid username or password"
                    )
                );

        if (!passwordEncoder.matches(
                password,
                admin.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "Invalid username or password"
            );
        }

        String token = jwtUtil.generateToken(
                admin.getUsername(),
                "SUPER_ADMIN"
        );

        return Map.of(
                "success", true,
                "message", "Superadmin login successful",
                "token", token,
                "username", admin.getUsername(),
                "role", "SUPER_ADMIN"
        );
    }

    // ================= SEND OTP =================

    public Map<String, Object> sendPasswordResetOtp() {

        SuperAdmin admin = getSuperAdmin();

        String otp = String.format(
                "%06d",
                SECURE_RANDOM.nextInt(1000000)
        );

        SuperAdminPasswordResetOtp otpRecord =
                otpRepository.findBySuperAdminId(admin.getId())
                .orElseGet(
                    SuperAdminPasswordResetOtp::new
                );

        otpRecord.setSuperAdminId(admin.getId());

        otpRecord.setOtpHash(
                passwordEncoder.encode(otp)
        );

        otpRecord.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );

        otpRecord.setAttempts(0);

        otpRecord.setVerifiedAt(null);

        otpRecord.setResetTokenHash(null);

        otpRecord.setResetTokenExpiresAt(null);

        otpRepository.save(otpRecord);

        String smsResponse = smsService.sendOtp(
                admin.getMobileNumber(),
                otp
        );

        validateSmsResponse(smsResponse);

        return Map.of(
                "success", true,
                "message", "OTP sent successfully",
                "mobile", maskMobile(admin.getMobileNumber()),
                "expiresInSeconds", 300
        );
    }

    // ================= VERIFY OTP =================

    public Map<String, Object> verifyPasswordResetOtp(
            String submittedOtp) {

        SuperAdmin admin = getSuperAdmin();

        SuperAdminPasswordResetOtp record =
                otpRepository.findBySuperAdminId(admin.getId())
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Please request an OTP first"
                    )
                );

        if (record.getExpiresAt().isBefore(
                LocalDateTime.now())) {

            otpRepository.delete(record);

            throw new IllegalArgumentException(
                    "OTP expired. Please request a new OTP"
            );
        }

        if (record.getAttempts() >= MAX_OTP_ATTEMPTS) {

            otpRepository.delete(record);

            throw new IllegalArgumentException(
                    "Maximum OTP attempts exceeded"
            );
        }

        if (!passwordEncoder.matches(
                submittedOtp,
                record.getOtpHash())) {

            record.setAttempts(
                    record.getAttempts() + 1
            );

            otpRepository.save(record);

            throw new IllegalArgumentException(
                    "Invalid OTP"
            );
        }

        // Generate secure reset token

        byte[] tokenBytes = new byte[32];

        SECURE_RANDOM.nextBytes(tokenBytes);

        String resetToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);

        record.setVerifiedAt(LocalDateTime.now());

        record.setResetTokenHash(
                sha256(resetToken)
        );

        record.setResetTokenExpiresAt(
                LocalDateTime.now().plusMinutes(10)
        );

        otpRepository.save(record);

        return Map.of(
                "success", true,
                "message", "OTP verified successfully",
                "resetToken", resetToken,
                "resetTokenExpiresInSeconds", 600
        );
    }

    // ================= RESET USERNAME AND PASSWORD =================

    @Transactional
    public Map<String, Object> resetCredentials(
            String resetToken,
            String newUsername,
            String newPassword) {

        SuperAdmin admin = getSuperAdmin();

        SuperAdminPasswordResetOtp record =
                otpRepository.findBySuperAdminId(admin.getId())
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Reset session not found. Request a new OTP"
                    )
                );

        // Verify OTP verification status and expiry

        if (record.getVerifiedAt() == null
                || record.getResetTokenHash() == null
                || record.getResetTokenExpiresAt() == null
                || record.getResetTokenExpiresAt()
                    .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Reset authorization expired. Verify a new OTP"
            );
        }

        // Validate reset token

        if (!MessageDigest.isEqual(
                record.getResetTokenHash()
                    .getBytes(StandardCharsets.UTF_8),

                sha256(resetToken)
                    .getBytes(StandardCharsets.UTF_8))) {

            throw new IllegalArgumentException(
                    "Invalid reset authorization"
            );
        }

        // Validate username

        if (newUsername == null || newUsername.isBlank()) {

            throw new IllegalArgumentException(
                    "Username is required"
            );
        }

        newUsername = newUsername.trim();

        if (newUsername.length() > 100) {

            throw new IllegalArgumentException(
                    "Username cannot exceed 100 characters"
            );
        }

        // Validate password

        if (newPassword == null
                || newPassword.length() < 8
                || newPassword.length() > 72) {

            throw new IllegalArgumentException(
                    "Password must be between 8 and 72 characters"
            );
        }

        // Check duplicate username

        superAdminRepository.findByUsername(newUsername)
                .filter(existing ->
                    !existing.getId().equals(admin.getId())
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Username already exists"
                    );
                });

        // Update username

        admin.setUsername(newUsername);

        // Update BCrypt password

        admin.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );

        superAdminRepository.save(admin);

        // Delete OTP and reset token after successful reset

        otpRepository.delete(record);

        return Map.of(
                "success", true,
                "message",
                "Superadmin username and password updated successfully",
                "username", newUsername
        );
    }

    // ================= GET SUPERADMIN =================

    private SuperAdmin getSuperAdmin() {

        return superAdminRepository
                .findFirstByOrderByIdAsc()
                .filter(SuperAdmin::isActive)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Superadmin account is not initialized"
                    )
                );
    }

    // ================= SMS RESPONSE VALIDATION =================

    private void validateSmsResponse(String response) {

        if (response == null ||
                response.startsWith("ERROR:")) {

            throw new IllegalStateException(
                    "Unable to send OTP. Please try again later"
            );
        }

        if ("TEST MODE ENABLED".equals(response)) {
            return;
        }

        try {

            JsonNode json = objectMapper.readTree(response);

            if (!json.path("return").asBoolean(false)) {

                throw new IllegalStateException(
                        "SMS provider did not accept the OTP"
                );
            }

        } catch (IllegalStateException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Unexpected response from SMS provider"
            );
        }
    }

    // ================= MASK MOBILE =================

    private String maskMobile(String mobile) {

        if (mobile == null || mobile.length() < 4) {
            return "****";
        }

        return "******" +
                mobile.substring(mobile.length() - 4);
    }

    // ================= SHA256 =================

    private String sha256(String value) {

        try {

            byte[] digest = MessageDigest
                    .getInstance("SHA-256")
                    .digest(
                        value.getBytes(StandardCharsets.UTF_8)
                    );

            return HexFormat.of().formatHex(digest);

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Unable to create reset authorization"
            );
        }
    }
}
