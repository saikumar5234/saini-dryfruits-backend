package com.example.demo.Repository;

import com.example.demo.model.SubAdminEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubAdminEmailRepository extends JpaRepository<SubAdminEmail, Long> {

    Optional<SubAdminEmail> findByEmail(String email);
}
