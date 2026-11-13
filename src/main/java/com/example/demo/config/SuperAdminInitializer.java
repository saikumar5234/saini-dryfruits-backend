package com.example.demo.config;

import com.example.demo.Repository.SuperAdminRepository;
import com.example.demo.model.SuperAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminInitializer implements CommandLineRunner {

    private final SuperAdminRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${superadmin.mobile}")
    private String superAdminMobile;

    // Supply these two values as environment variables for the first deployment only.
    @Value("${superadmin.initial.username:}")
    private String initialUsername;

    @Value("${superadmin.initial.password:}")
    private String initialPassword;

    public SuperAdminInitializer(
            SuperAdminRepository repository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Never overwrite an existing Superadmin on application restart.
        if (repository.count() > 0) {
            return;
        }

        if (initialUsername == null || initialUsername.isBlank()
                || initialPassword == null || initialPassword.isBlank()) {
            throw new IllegalStateException(
                    "No Superadmin exists. Set SUPERADMIN_INITIAL_USERNAME and SUPERADMIN_INITIAL_PASSWORD once to initialize it.");
        }

        SuperAdmin admin = new SuperAdmin();
        admin.setUsername(initialUsername.trim());
        admin.setPasswordHash(passwordEncoder.encode(initialPassword));
        admin.setMobileNumber(superAdminMobile.trim());
        admin.setActive(true);
        repository.save(admin);
    }
}
