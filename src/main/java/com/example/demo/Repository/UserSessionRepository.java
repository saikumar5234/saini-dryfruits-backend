package com.example.demo.Repository;

import com.example.demo.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    List<UserSession> findByUserIdOrderByCreatedAtDesc(String userId);
    
    @Query("SELECT us FROM UserSession us WHERE us.userId = :userId ORDER BY us.createdAt DESC")
    List<UserSession> findRecentSessionsByUserId(@Param("userId") String userId, @Param("limit") int limit);
    
    @Query("SELECT COUNT(us) FROM UserSession us WHERE us.userId = :userId")
    Long countSessionsByUserId(@Param("userId") String userId);
}