package com.example.demo.model;



import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for storing push tokens
 */
@Entity
@Table(name = "push_tokens", indexes = {
    @Index(name = "idx_user_platform", columnList = "user_id,platform"),
    @Index(name = "idx_active", columnList = "active")
})
public class PushToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "push_token", nullable = false, length = 500)
    private String pushToken;
    
    @Column(name = "platform", nullable = false, length = 20)
    private String platform; // "android" or "ios"
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public PushToken() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPushToken() {
        return pushToken;
    }
    
    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
