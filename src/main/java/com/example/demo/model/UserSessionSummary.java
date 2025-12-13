package com.example.demo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_session_summary")
public class UserSessionSummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;
    
    @Column(name = "user_gst_number", length = 15, nullable = true)
    private String userGstNumber;
    
    @Column(name = "user_mobile")
    private String userMobile;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(name = "total_sessions")
    private Integer totalSessions = 0;
    
    @Column(name = "total_time_spent")
    private Integer totalTimeSpent = 0;
    
    @Column(name = "last_session_date")
    private LocalDateTime lastSessionDate;
    
    @Column(name = "first_session_date")
    private LocalDateTime firstSessionDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getUserId() { 
        return userId; 
    }
    
    public void setUserId(String userId) { 
        this.userId = userId; 
    }
    
    public String getUserGstNumber() { 
        return userGstNumber; 
    }
    
    public void setUserGstNumber(String userGstNumber) { 
        this.userGstNumber = userGstNumber; 
    }
    
    public String getUserMobile() { 
        return userMobile; 
    }
    
    public void setUserMobile(String userMobile) { 
        this.userMobile = userMobile; 
    }
    
    public String getUserName() { 
        return userName; 
    }
    
    public void setUserName(String userName) { 
        this.userName = userName; 
    }
    
    public Integer getTotalSessions() { 
        return totalSessions; 
    }
    
    public void setTotalSessions(Integer totalSessions) { 
        this.totalSessions = totalSessions; 
    }
    
    public Integer getTotalTimeSpent() { 
        return totalTimeSpent; 
    }
    
    public void setTotalTimeSpent(Integer totalTimeSpent) { 
        this.totalTimeSpent = totalTimeSpent; 
    }
    
    public LocalDateTime getLastSessionDate() { 
        return lastSessionDate; 
    }
    
    public void setLastSessionDate(LocalDateTime lastSessionDate) { 
        this.lastSessionDate = lastSessionDate; 
    }
    
    public LocalDateTime getFirstSessionDate() { 
        return firstSessionDate; 
    }
    
    public void setFirstSessionDate(LocalDateTime firstSessionDate) { 
        this.firstSessionDate = firstSessionDate; 
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
