
package com.example.demo.services;

import com.example.demo.Repository.SubAdminEmailRepository;
import com.example.demo.Repository.AdminSignupRequestRepository;
import com.example.demo.model.SubAdminEmail;
import com.example.demo.model.AdminSignupRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.NoSuchElementException;

@Service
public class SubAdminManagementService {

    private final SubAdminEmailRepository subAdminEmailRepository;
    private final AdminSignupRequestRepository adminSignupRequestRepository;

    public SubAdminManagementService(
            SubAdminEmailRepository subAdminEmailRepository,
            AdminSignupRequestRepository adminSignupRequestRepository) {

        this.subAdminEmailRepository = subAdminEmailRepository;
        this.adminSignupRequestRepository = adminSignupRequestRepository;
    }

    @Transactional
    public void deleteSubAdmin(Long id) {

        // 1. Find the actual dashboard record
        SubAdminEmail subAdminEmail = subAdminEmailRepository
                .findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Subadmin not found with ID: " + id
                    )
                );

        String email = subAdminEmail.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        // 2. Find registered Subadmin account using email
        AdminSignupRequest registeredAdmin =
                adminSignupRequestRepository.findByEmail(email)
                .orElse(null);

        // 3. Delete registered login account if it exists
        if (registeredAdmin != null) {

            String role = registeredAdmin.getRole() == null
                    ? ""
                    : registeredAdmin.getRole()
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replace("-", "_");

            if (!role.equals("ADMIN")
                    && !role.equals("SUB_ADMIN")
                    && !role.equals("SUBADMIN")) {

                throw new IllegalArgumentException(
                        "This account is not a Subadmin account"
                );
            }

            adminSignupRequestRepository.delete(registeredAdmin);
        }

        // 4. Delete the dashboard email record
        subAdminEmailRepository.delete(subAdminEmail);
    }
}
