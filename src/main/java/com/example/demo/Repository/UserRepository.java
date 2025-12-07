package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Status;
import com.example.demo.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
//    Optional<User> findByMobile(String mobile);
//    Optional<User> findByEmail(String email);
//    Optional<User> findByMobileOrEmail(String mobile, String email);
//    List<User> findByStatus(Status status);
//    boolean existsByMobile(String mobile);
//    boolean existsByEmail(String email);
	 Optional<User> findByMobile(String mobile);
	 Optional<User> findByGstNumber(String gstNumber);
	 List<User> findByStatus(Status status);
}