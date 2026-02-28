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
     * If token exists for user and platform, update it; otherwise create new
     */
    public PushToken registerPushToken(String pushToken, String platform, String userId) {
        // Check if token already exists for this user and platform
        Optional<PushToken> existingToken = pushTokenRepository
            .findByUserIdAndPlatform(userId, platform);
        
        PushToken token;
        if (existingToken.isPresent()) {
            // Update existing token
            token = existingToken.get();
            token.setPushToken(pushToken);
            token.setUpdatedAt(LocalDateTime.now());
            token.setActive(true);
        } else {
            // Create new token
            token = new PushToken();
            token.setPushToken(pushToken);
            token.setPlatform(platform);
            token.setUserId(userId);
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
