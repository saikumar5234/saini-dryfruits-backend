package com.example.demo.Repository;



import com.example.demo.model.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PushToken entity
 */
@Repository
public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    
    /**
     * Find active push token by user ID and platform
     */
    Optional<PushToken> findByUserIdAndPlatform(String userId, String platform);
    
    /**
     * Find all active push tokens for a user
     */
    List<PushToken> findByUserIdAndActiveTrue(String userId);
    
    /**
     * Find all active push tokens
     */
    List<PushToken> findByActiveTrue();
    
    /**
     * Find push tokens for multiple users
     */
    List<PushToken> findByUserIdInAndActiveTrue(List<String> userIds);
    
    /**
     * Find by push token string
     */
    Optional<PushToken> findByPushToken(String pushToken);
}
