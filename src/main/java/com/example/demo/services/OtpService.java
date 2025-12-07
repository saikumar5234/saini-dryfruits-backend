//package com.example.demo.services;
//
//import java.time.Instant;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriComponentsBuilder;
//
//@Service
//public class OtpService {
//
//    @Value("${sms.apiKey}")
//    private String smsApiKey;
//
//    @Value("${sms.senderId}")
//    private String senderId;
//
//    @Value("${sms.templateId}")
//    private String templateId;
//
//    @Value("${sms.baseUrl}")
//    private String smsBaseUrl;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final OtpRepository otpRepository;  // your JPA repo for OTP records
//
//    public void sendOtp(String mobile, String gstNumber, String type) {
//        String identifier = (mobile != null && !mobile.isEmpty()) ? mobile : gstNumber;
//
//        // 6-digit random
//        String otp = String.format("%06d", new java.util.Random().nextInt(1_000_000));
//
//        // save in DB (hash if you want extra security)
//        OtpEntity entity = new OtpEntity();
//        entity.setIdentifier(identifier);
//        entity.setOtp(otp); // or hash(otp)
//        entity.setType(type);
//        entity.setExpiryTime(Instant.now().plusSeconds(300)); // 5 minutes
//        entity.setAttempts(0);
//        otpRepository.save(entity);
//
//        // Build SMS text using your DLT template variables
//        String message = "Your OTP for Saini Meva Stores is " + otp + ". It is valid for 5 minutes.";
//
//        // Example Fast2SMS/Phase2SMS-style call (adjust to your provider)
//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(smsBaseUrl)
//                .queryParam("authorization", smsApiKey)
//                .queryParam("sender_id", senderId)
//                .queryParam("message", message)
//                .queryParam("template_id", templateId)
//                .queryParam("route", "dlt")
//                .queryParam("numbers", mobile); // if sending to mobile
//
//        restTemplate.getForObject(builder.toUriString(), String.class);
//    }
//
//    public boolean verifyOtp(String mobile, String gstNumber, String type, String otp) {
//        String identifier = (mobile != null && !mobile.isEmpty()) ? mobile : gstNumber;
//
//        OtpEntity entity = otpRepository
//                .findTopByIdentifierAndTypeOrderByCreatedAtDesc(identifier, type)
//                .orElseThrow(() -> new RuntimeException("OTP not found"));
//
//        if (entity.getExpiryTime().isBefore(Instant.now())) {
//            throw new RuntimeException("OTP expired");
//        }
//
//        if (entity.getAttempts() >= 5) {
//            throw new RuntimeException("Too many attempts");
//        }
//
//        entity.setAttempts(entity.getAttempts() + 1);
//        otpRepository.save(entity);
//
//        if (!entity.getOtp().equals(otp)) { // or compare hashes
//            throw new RuntimeException("Invalid OTP");
//        }
//
//        // mark as used
//        entity.setUsed(true);
//        otpRepository.save(entity);
//        return true;
//    }
//}