package com.example.demo.services;



import com.example.demo.model.PushToken;
import com.example.demo.Repository.PushTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing push tokens
 */
@Service
@Transactional
public class PushTokenService {
    
    @Autowired
    private PushTokenRepository pushTokenRepository;
    
    /**
     * Register or update push token for a user
     * Upsert by pushToken (device identity). This avoids overwriting tokens
     * when a user logs in on multiple devices with the same platform.
     */
    public PushToken registerPushToken(String pushToken, String platform, String userId) {
        if (pushToken == null || pushToken.trim().isEmpty()) {
            throw new IllegalArgumentException("pushToken is required");
        }
        if (platform == null || platform.trim().isEmpty()) {
            throw new IllegalArgumentException("platform is required");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId is required");
        }

        String normalizedToken = pushToken.trim();
        String normalizedPlatform = platform.trim().toLowerCase();
        String normalizedUserId = userId.trim();

        // Upsert by token string (preferred)
        Optional<PushToken> existingToken = pushTokenRepository.findByPushToken(normalizedToken);
        
        PushToken token;
        if (existingToken.isPresent()) {
            // Update existing token
            token = existingToken.get();
            token.setPushToken(normalizedToken);
            token.setPlatform(normalizedPlatform);
            token.setUserId(normalizedUserId);
            token.setUpdatedAt(LocalDateTime.now());
            token.setActive(true);
        } else {
            // Create new token
            token = new PushToken();
            token.setPushToken(normalizedToken);
            token.setPlatform(normalizedPlatform);
            token.setUserId(normalizedUserId);
            token.setActive(true);
            token.setCreatedAt(LocalDateTime.now());
            token.setUpdatedAt(LocalDateTime.now());
        }
        
        return pushTokenRepository.save(token);
    }
    
    /**
     * Get all active push tokens for a user
     */
    public List<PushToken> getUserPushTokens(String userId) {
        return pushTokenRepository.findByUserIdAndActiveTrue(userId);
    }
    
    /**
     * Get all active push tokens (for sending to all users)
     */
    public List<PushToken> getAllActivePushTokens() {
        return pushTokenRepository.findByActiveTrue();
    }
    
    /**
     * Deactivate push token (soft delete)
     */
    public void deactivatePushToken(Long tokenId) {
        Optional<PushToken> token = pushTokenRepository.findById(tokenId);
        if (token.isPresent()) {
            token.get().setActive(false);
            token.get().setUpdatedAt(LocalDateTime.now());
            pushTokenRepository.save(token.get());
        }
    }

    /**
     * Deactivate by push token string (soft delete)
     */
    public void deactivateByPushToken(String pushToken) {
        if (pushToken == null || pushToken.trim().isEmpty()) return;

        pushTokenRepository.findByPushToken(pushToken.trim()).ifPresent(token -> {
            token.setActive(false);
            token.setUpdatedAt(LocalDateTime.now());
            pushTokenRepository.save(token);
        });
    }
    
    /**
     * Delete push token (hard delete)
     */
    public void deletePushToken(Long tokenId) {
        pushTokenRepository.deleteById(tokenId);
    }
    
    /**
     * Get push tokens for multiple users
     */
    public List<PushToken> getPushTokensForUsers(List<String> userIds) {
        return pushTokenRepository.findByUserIdInAndActiveTrue(userIds);
    }
}
