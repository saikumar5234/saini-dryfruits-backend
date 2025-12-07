package com.example.demo.model;

import com.example.demo.model.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_signup_requests")
public class AdminSignupRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String passwordHash;
    @Column(nullable = false)
    private String role;

    public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	@Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

//    private String ipAddress;
//    private String deviceInfo;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
//	public String getIpAddress() {
//		return ipAddress;
//	}
//	public void setIpAddress(String ipAddress) {
//		this.ipAddress = ipAddress;
//	}
//	public String getDeviceInfo() {
//		return deviceInfo;
//	}
//	public void setDeviceInfo(String deviceInfo) {
//		this.deviceInfo = deviceInfo;
//	}

   
}
