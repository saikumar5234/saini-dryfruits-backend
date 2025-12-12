package com.example.demo.Repository;

import com.example.demo.model.BannerText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BannerTextRepository extends JpaRepository<BannerText, Long> {
    
    Optional<BannerText> findByIsActiveTrue();
    
    Optional<BannerText> findFirstByIsActiveTrueOrderByUpdatedAtDesc();
}
