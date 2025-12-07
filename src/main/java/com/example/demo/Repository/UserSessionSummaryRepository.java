package com.example.demo.Repository;

import com.example.demo.model.UserSessionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionSummaryRepository extends JpaRepository<UserSessionSummary, Long> {
    
    Optional<UserSessionSummary> findByUserId(String userId);
    
    @Query("SELECT uss FROM UserSessionSummary uss ORDER BY uss.totalTimeSpent DESC")
    List<UserSessionSummary> findAllOrderByTotalTimeSpentDesc();
    
    @Query("SELECT COUNT(uss) FROM UserSessionSummary uss")
    Long countTotalUsers();
    
    @Query("SELECT SUM(uss.totalSessions) FROM UserSessionSummary uss")
    Long sumTotalSessions();
    
    @Query("SELECT SUM(uss.totalTimeSpent) FROM UserSessionSummary uss")
    Long sumTotalTimeSpent();
}