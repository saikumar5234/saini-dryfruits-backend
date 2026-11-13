package com.example.demo.Repository;

import com.example.demo.model.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {
    Optional<SuperAdmin> findByUsername(String username);
    Optional<SuperAdmin> findFirstByOrderByIdAsc();
}
