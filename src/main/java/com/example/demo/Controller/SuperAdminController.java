
package com.example.demo.Controller;

import com.example.demo.dto.SuperAdminLoginRequest;
import com.example.demo.dto.SuperAdminResetPasswordRequest;
import com.example.demo.dto.SuperAdminVerifyOtpRequest;
import com.example.demo.services.SuperAdminService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/superadmin")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    public SuperAdminController(
            SuperAdminService superAdminService) {

        this.superAdminService = superAdminService;
    }

    // ================= LOGIN =================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody SuperAdminLoginRequest request) {

        try {

            return ResponseEntity.ok(
                    superAdminService.login(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", ex.getMessage()
                    ));
        }
    }

    // ================= FORGOT PASSWORD =================

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword() {

        try {

            return ResponseEntity.ok(
                    superAdminService.sendPasswordResetOtp()
            );

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of(
                            "success", false,
                            "message", ex.getMessage()
                    ));
        }
    }

    // ================= VERIFY OTP =================

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @Valid @RequestBody SuperAdminVerifyOtpRequest request) {

        try {

            return ResponseEntity.ok(
                    superAdminService.verifyPasswordResetOtp(
                            request.getOtp()
                    )
            );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", ex.getMessage()
                    ));
        }
    }

    // ================= RESET USERNAME AND PASSWORD =================

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody SuperAdminResetPasswordRequest request) {

        try {

            return ResponseEntity.ok(
                    superAdminService.resetCredentials(
                            request.getResetToken(),
                            request.getNewUsername(),
                            request.getNewPassword()
                    )
            );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", ex.getMessage()
                    ));
        }
    }
}
