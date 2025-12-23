package com.example.demo.Controller;

import com.example.demo.Repository.GreetingRepository;
import com.example.demo.Repository.ProductRepository;
import com.example.demo.Repository.SubAdminEmailRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.UserSessionRepository;
import com.example.demo.Repository.UserSessionSummaryRepository;
import com.example.demo.config.JwtUtil;
import com.example.demo.dto.AdminLoginDTO;
import com.example.demo.dto.AdminSignupRequestDTO;
import com.example.demo.dto.BannerTextRequest;
import com.example.demo.dto.BannerTextResponse;
import com.example.demo.dto.CreateProductRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.ProductResponseDTO;
import com.example.demo.dto.ProductUpdateRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.Repository.ProductImageRepository;
import com.example.demo.Repository.ProductPriceHistoryRepository;
import com.example.demo.model.AdminSignupRequest;
import com.example.demo.model.Greeting;
import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.model.ProductPriceHistory;
import com.example.demo.model.Status;
import com.example.demo.model.SubAdminEmail;
import com.example.demo.model.User;
import com.example.demo.model.UserSession;
import com.example.demo.model.UserSessionSummary;
import com.example.demo.services.AdminLoginService;
import com.example.demo.services.AdminOtpService;
import com.example.demo.services.AdminSignupRequestService;
import com.example.demo.services.BannerTextService;
import com.example.demo.services.SmsService;
import com.example.demo.services.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
//Add these imports at the top if not already present
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class Controller {
	
	 @Autowired
	    private SmsService smsService;
	 @Autowired
	    private AdminSignupRequestService adminSignupRequestService;
	 @Autowired
	    private AdminLoginService adminLoginService;
	 @Autowired
	    private BannerTextService bannerTextService;
	 @Autowired
	    private UserService userService;
	 @Autowired
	    private AdminOtpService otpService;

	    @Autowired
	    private JwtUtil jwtUtil;
	    @Autowired
	    private SubAdminEmailRepository subAdminEmailRepository;

	    


	private static final Pattern GST_PATTERN = Pattern.compile("^[0-9A-Z]{15}$");
	private ProductPriceHistoryRepository priceHistoryRepository;
    private final GreetingRepository greetingRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final UserSessionSummaryRepository userSessionSummaryRepository;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Map<String, Long> otpExpiry = new ConcurrentHashMap<>();
    private static final long OTP_EXPIRY_TIME = 5 * 60 * 1000; // 5 minutes

    // Helper method to build OTP key
    protected String buildOtpKey(String mobile, String gstNumber, String purpose) {
        // Normalize inputs
        if (mobile != null) mobile = mobile.trim();
        if (gstNumber != null) gstNumber = gstNumber != null ? gstNumber.trim().toUpperCase() : null;
        if (purpose == null) purpose = "signup";
        
        if (mobile != null && !mobile.isEmpty()) {
            return purpose + "_M_" + mobile;
        }
        if (gstNumber != null && !gstNumber.isEmpty()) {
            return purpose + "_G_" + gstNumber;
        }
        throw new IllegalArgumentException("Either mobile or gstNumber is required");
    }
    public Controller(
    	    GreetingRepository greetingRepository,
    	    ProductRepository productRepository,
    	    ProductImageRepository productImageRepository,
    	    ProductPriceHistoryRepository priceHistoryRepository ,
    	    UserRepository userRepository,
    	    UserSessionRepository userSessionRepository,           
    	    UserSessionSummaryRepository userSessionSummaryRepository
    	) {
    	    this.greetingRepository = greetingRepository;
    	    this.productRepository = productRepository;
    	    this.productImageRepository = productImageRepository;
    	    this.priceHistoryRepository = priceHistoryRepository;
    	    this.userRepository = userRepository;
    	    this.userSessionRepository = userSessionRepository;          
    	    this.userSessionSummaryRepository = userSessionSummaryRepository;  
    	}
    
    @PostMapping("/admin/send-otp")
    public Map<String, String> sendOtp(@RequestParam String email) {
        otpService.sendOtp(email);
        return Map.of("message", "OTP sent successfully");
    }

    @PostMapping("/admin/verify-otp")
    public Map<String, Object> verifyOtp(
            @RequestParam String email,
            @RequestParam String mobile,
            @RequestParam String otp
    ) {
        AdminSignupRequest admin = otpService.verifyOtp(email, mobile, otp);
        String token = jwtUtil.generateToken(admin.getEmail(), admin.getRole());

        return Map.of(
                "token", token,
                "email", admin.getEmail(),
                "role", admin.getRole()
        );
    }
    
    @PostMapping("/admin/sub-admins")
    public Map<String, Object> createSubAdminEmail(@RequestBody Map<String, String> req) {

        String email = req.get("email");

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        subAdminEmailRepository.findByEmail(email).ifPresent(e -> {
            throw new RuntimeException("Email already exists");
        });

        SubAdminEmail sub = new SubAdminEmail();
        sub.setEmail(email.trim().toLowerCase());

        subAdminEmailRepository.save(sub);

        return Map.of(
                "success", true,
                "email", email,
                "message", "Sub-admin email created successfully"
        );
    }

    // LIST SUB-ADMIN EMAILS
    @GetMapping("/admin/sub-admins")
    public Object listSubAdminEmails() {
        return subAdminEmailRepository.findAll();
    }
    @GetMapping("/banner-text")
    public ResponseEntity<BannerTextResponse> getBannerText() {
        try {
            String textJson = bannerTextService.getActiveBannerText();
            
            BannerTextResponse response = new BannerTextResponse();
            response.setSuccess(true);
            
            // Parse JSON string to Map
            Map<String, String> textMap = parseJsonToMap(textJson);
            response.setText(textMap);
            response.setMessage("Banner text retrieved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            BannerTextResponse response = new BannerTextResponse();
            response.setSuccess(false);
            
            // Default multilingual text
            Map<String, String> defaultText = new HashMap<>();
            defaultText.put("en", "Welcome to Saini Mewa Stores - Your trusted source for premium dry fruits! 🥜✨");
            defaultText.put("hi", "सैनी मेवा स्टोर्स में आपका स्वागत है - प्रीमियम सूखे मेवों का आपका विश्वसनीय स्रोत! 🥜✨");
            defaultText.put("te", "సైని మేవా స్టోర్‌లకు స్వాగతం - ప్రీమియం ఎండిన పండ్లకు మీ విశ్వసనీయ మూలం! 🥜✨");
            
            response.setText(defaultText);
            response.setMessage("Error retrieving banner text: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper method to parse JSON string to Map
    private Map<String, String> parseJsonToMap(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            // If parsing fails, return empty map or default
            Map<String, String> defaultMap = new HashMap<>();
            defaultMap.put("en", jsonString); // Fallback to original string as English
            return defaultMap;
        }
    }
    
    @PostMapping("/banner-text")
    public ResponseEntity<BannerTextResponse> createOrUpdateBannerText(@RequestBody BannerTextRequest request) {
        try {
            // Validate multilingual text
            Map<String, String> textMap = request.getText();
            if (textMap == null || textMap.isEmpty()) {
                BannerTextResponse response = new BannerTextResponse();
                response.setSuccess(false);
                response.setMessage("Text cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate that at least English text is provided
            String englishText = textMap.get("en");
            if (englishText == null || englishText.trim().isEmpty()) {
                BannerTextResponse response = new BannerTextResponse();
                response.setSuccess(false);
                response.setMessage("English text (en) is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Convert Map to JSON string
            String textJson = request.getTextJsonString();
            
            // Create or update banner text
            bannerTextService.createOrUpdateBannerText(textJson, request.getIsActive());
            
            BannerTextResponse response = new BannerTextResponse();
            response.setSuccess(true);
            response.setText(textMap); // Return multilingual map
            response.setMessage("Banner text updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            BannerTextResponse response = new BannerTextResponse();
            response.setSuccess(false);
            response.setMessage("Error updating banner text: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/admin-signup")
    public ResponseEntity<Map<String, Object>> adminSignup(@RequestBody AdminSignupRequestDTO dto) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate required fields
            if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "First name is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Last name is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (dto.getMobile() == null || dto.getMobile().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Mobile number is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Password is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (dto.getPassword().length() < 6) {
                response.put("success", false);
                response.put("error", "Password must be at least 6 characters long");
                return ResponseEntity.badRequest().body(response);
            }

            // Create admin signup request
            AdminSignupRequest request = adminSignupRequestService.createAdminSignupRequest(dto);
            
            response.put("success", true);
            response.put("message", "Admin signup request submitted successfully. Waiting for approval.");
            response.put("id", request.getId());
            response.put("email", request.getEmail());
            response.put("status", request.getStatus().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "An error occurred while processing your request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    
    @PostMapping("/admin-login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody AdminLoginDTO loginDTO) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate required fields
            if (loginDTO.getEmail() == null || loginDTO.getEmail().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (loginDTO.getPassword() == null || loginDTO.getPassword().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Password is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Authenticate admin
            AdminSignupRequest admin = adminLoginService.authenticateAdmin(loginDTO);
            
            // Build success response
            response.put("success", true);
            response.put("message", "Admin login successful");
            response.put("id", admin.getId());
            response.put("email", admin.getEmail());
            response.put("firstName", admin.getFirstName());
            response.put("lastName", admin.getLastName());
            response.put("mobile", admin.getMobile());
            response.put("role", admin.getRole());
            response.put("status", admin.getStatus().toString());
            response.put("isAdmin", true);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "An error occurred while processing your request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody SignupRequest req) {
        Map<String, Object> resp = new HashMap<>();
        
        // Manual validation
        if (req.firstName == null || req.firstName.trim().isEmpty()) {
            resp.put("success", false);
            resp.put("error", "First name is required");
            return ResponseEntity.badRequest().body(resp);
        }
        
        if (req.lastName == null || req.lastName.trim().isEmpty()) {
            resp.put("success", false);
            resp.put("error", "Last name is required");
            return ResponseEntity.badRequest().body(resp);
        }
        
        if (req.mobile == null || req.mobile.length() != 10) {
            resp.put("success", false);
            resp.put("error", "Invalid mobile number. Must be 10 digits");
            return ResponseEntity.badRequest().body(resp);
        }
        
        // Validate mobile contains only digits
        if (!req.mobile.matches("^[0-9]{10}$")) {
            resp.put("success", false);
            resp.put("error", "Mobile number must contain only digits");
            return ResponseEntity.badRequest().body(resp);
        }
        
        // GST number is OPTIONAL - validate only if provided
        String gstNumber = null;
        if (req.gstNumber != null && !req.gstNumber.trim().isEmpty()) {
            gstNumber = req.gstNumber.trim().toUpperCase();
            
            // Validate GST number format (15 alphanumeric characters)
            if (gstNumber.length() != 15) {
                resp.put("success", false);
                resp.put("error", "GST number must be exactly 15 characters");
                return ResponseEntity.badRequest().body(resp);
            }
            
            if (!GST_PATTERN.matcher(gstNumber).matches()) {
                resp.put("success", false);
                resp.put("error", "Invalid GST number format. Must be 15 alphanumeric characters");
                return ResponseEntity.badRequest().body(resp);
            }
        }
        
        if (req.password == null || req.password.length() < 6) {
            resp.put("success", false);
            resp.put("error", "Password must be at least 6 characters");
            return ResponseEntity.badRequest().body(resp);
        }
        
        // Check if mobile already exists
        if (userRepository.findByMobile(req.mobile).isPresent()) {
            resp.put("success", false);
            resp.put("error", "Mobile number already exists");
            return ResponseEntity.badRequest().body(resp);
        }
        
        // Check if GST number already exists (only if GST is provided)
        if (gstNumber != null && userRepository.findByGstNumber(gstNumber).isPresent()) {
            resp.put("success", false);
            resp.put("error", "GST number already exists");
            return ResponseEntity.badRequest().body(resp);
        }
        
        try {
            // Create new user
            User user = new User();
            user.setFirstName(req.firstName.trim());
            user.setLastName(req.lastName.trim());
            user.setMobile(req.mobile);
            user.setGstNumber(gstNumber);  // Can be null now
            user.setPasswordHash(passwordEncoder.encode(req.password));
            user.setStatus(Status.PENDING);
            
            User savedUser = userRepository.save(user);
            
            // Create initial UserSessionSummary for new user
            UserSessionSummary initialSummary = new UserSessionSummary();
            initialSummary.setUserId(savedUser.getId().toString());
            initialSummary.setUserGstNumber(savedUser.getGstNumber());  // Can be null
            initialSummary.setUserMobile(savedUser.getMobile());
            initialSummary.setUserName(savedUser.getFirstName() + " " + savedUser.getLastName());
            initialSummary.setTotalSessions(0);
            initialSummary.setTotalTimeSpent(0);
            initialSummary.setLastSessionDate(null);
            initialSummary.setFirstSessionDate(null);
            initialSummary.setCreatedAt(LocalDateTime.now());
            initialSummary.setUpdatedAt(LocalDateTime.now());
            userSessionSummaryRepository.save(initialSummary);
            
            // Build user response
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", savedUser.getId());
            userData.put("firstName", savedUser.getFirstName());
            userData.put("lastName", savedUser.getLastName());
            userData.put("gstNumber", savedUser.getGstNumber() != null ? savedUser.getGstNumber() : "");
            userData.put("mobile", savedUser.getMobile());
            userData.put("status", savedUser.getStatus().toString());
            userData.put("isApproved", savedUser.getStatus() == Status.APPROVED);
            userData.put("approved", savedUser.getStatus() == Status.APPROVED);
            userData.put("createdAt", savedUser.getCreatedAt().toString());
            userData.put("registrationDate", savedUser.getCreatedAt().toString());
            
            resp.put("success", true);
            resp.put("message", "User registered successfully. Please login to continue.");
            resp.put("userId", savedUser.getId());
            resp.put("user", userData);
            
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("error", "Registration failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
    
    @PostMapping("/check-user-exists")
    public ResponseEntity<Map<String, Object>> checkUserExists(@RequestBody Map<String, String> request) {
        Map<String, Object> resp = new HashMap<>();
        
        try {
            String mobile = request.get("mobile");
            String gstNumber = request.get("gstNumber");
            
            // Check if at least one identifier is provided
            if ((mobile == null || mobile.trim().isEmpty()) && 
                (gstNumber == null || gstNumber.trim().isEmpty())) {
                resp.put("exists", false);
                resp.put("error", "Mobile number or GST number is required");
                return ResponseEntity.badRequest().body(resp);
            }
            
            boolean exists = false;
            User user = null;
            
            // Check by mobile if provided
            if (mobile != null && !mobile.trim().isEmpty()) {
                mobile = mobile.trim();
                Optional<User> userOpt = userRepository.findByMobile(mobile);
                if (userOpt.isPresent()) {
                    exists = true;
                    user = userOpt.get();
                    System.out.println("User found by mobile: " + mobile + ", ID: " + user.getId());
                } else {
                    System.out.println("User NOT found by mobile: " + mobile);
                }
            }
            
            // Check by GST if mobile not found and GST is provided
            if (!exists && gstNumber != null && !gstNumber.trim().isEmpty()) {
                gstNumber = gstNumber.trim().toUpperCase();
                Optional<User> userOpt = userRepository.findByGstNumber(gstNumber);
                if (userOpt.isPresent()) {
                    exists = true;
                    user = userOpt.get();
                    System.out.println("User found by GST: " + gstNumber + ", ID: " + user.getId());
                } else {
                    System.out.println("User NOT found by GST: " + gstNumber);
                }
            }
            
            resp.put("exists", exists);
            
            // Optionally return user data if found
            if (exists && user != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("mobile", user.getMobile() != null ? user.getMobile() : "");
                userData.put("gstNumber", user.getGstNumber() != null ? user.getGstNumber() : "");
                userData.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
                userData.put("lastName", user.getLastName() != null ? user.getLastName() : "");
                resp.put("user", userData);
            }
            
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("exists", false);
            resp.put("error", "Error checking user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> resp = new HashMap<>();
        
        String gstNumber = request.get("gstNumber");
        String mobile = request.get("mobile");
        String newPassword = request.get("newPassword");
        
        try {
            User user = null;
            
            if (gstNumber != null && !gstNumber.isEmpty()) {
                user = userRepository.findByGstNumber(gstNumber.toUpperCase()).orElse(null);
            } else if (mobile != null && !mobile.isEmpty()) {
                user = userRepository.findByMobile(mobile).orElse(null);
            }
            
            if (user == null) {
                resp.put("success", false);
                resp.put("error", "User not found");
                return ResponseEntity.badRequest().body(resp);
            }
            
            // Update password
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            resp.put("success", true);
            resp.put("message", "Password reset successfully");
            
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("error", "Failed to reset password");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req) {
        Map<String, Object> resp = new HashMap<>();
        
        try {
            // Validate input
            if ((req.mobile == null || req.mobile.trim().isEmpty()) && 
                (req.gstNumber == null || req.gstNumber.trim().isEmpty())) {
                resp.put("success", false);
                resp.put("error", "Mobile number or GST number is required");
                return ResponseEntity.badRequest().body(resp);
            }
            
            if (req.password == null || req.password.isEmpty()) {
                resp.put("success", false);
                resp.put("error", "Password is required");
                return ResponseEntity.badRequest().body(resp);
            }
            
            // Find user by mobile or GST
            User user = null;
            if (req.mobile != null && !req.mobile.trim().isEmpty()) {
                user = userRepository.findByMobile(req.mobile.trim()).orElse(null);
            } else if (req.gstNumber != null && !req.gstNumber.trim().isEmpty()) {
                user = userRepository.findByGstNumber(req.gstNumber.trim().toUpperCase()).orElse(null);
            }
            
            if (user == null) {
                resp.put("success", false);
                resp.put("error", "Invalid credentials");
                return ResponseEntity.badRequest().body(resp);
            }
            
            // Verify password
            if (!passwordEncoder.matches(req.password, user.getPasswordHash())) {
                resp.put("success", false);
                resp.put("error", "Invalid credentials");
                return ResponseEntity.badRequest().body(resp);
            }
            
            // Generate token (use your existing JWT service or simple token)
            String token = "authenticated";  // Replace with your JWT token generation
            
            // Build user response - handle null values properly
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
            userData.put("lastName", user.getLastName() != null ? user.getLastName() : "");
            userData.put("gstNumber", user.getGstNumber() != null ? user.getGstNumber() : "");
            userData.put("mobile", user.getMobile() != null ? user.getMobile() : "");
            userData.put("status", user.getStatus() != null ? user.getStatus().toString() : "PENDING");
            userData.put("isApproved", user.getStatus() != null && user.getStatus() == Status.APPROVED);
            userData.put("approved", user.getStatus() != null && user.getStatus() == Status.APPROVED);
            
            // Handle dates
            if (user.getCreatedAt() != null) {
                userData.put("createdAt", user.getCreatedAt().toString());
                userData.put("registrationDate", user.getCreatedAt().toString());
            } else {
                userData.put("createdAt", "");
                userData.put("registrationDate", "");
            }
            
            // Build response
            resp.put("success", true);
            resp.put("token", token);
            resp.put("user", userData);
            
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
    @PostMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate that at least one identifier is provided
            if ((request.getMobile() == null || request.getMobile().isEmpty()) &&
                (request.getGstNumber() == null || request.getGstNumber().isEmpty())) {
                response.put("success", false);
                response.put("error", "Either mobile number or GST number is required for identification");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Find user by mobile or GST
            Optional<User> userOptional = Optional.empty();
            
            if (request.getMobile() != null && !request.getMobile().isEmpty()) {
                userOptional = userService.findByMobile(request.getMobile());
            } else if (request.getGstNumber() != null && !request.getGstNumber().isEmpty()) {
                userOptional = userService.findByGstNumber(request.getGstNumber());
            }
            
            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            User user = userOptional.get();
            
            // Update first name and last name
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            
            // Update GST number if provided
            if (request.getNewGstNumber() != null && !request.getNewGstNumber().isEmpty()) {
                // Check if the new GST number is different from current
                if (user.getGstNumber() != null && 
                    user.getGstNumber().equalsIgnoreCase(request.getNewGstNumber())) {
                    // Same GST number, no need to check for duplicates
                } else {
                    // Check if new GST number already exists (if different from current)
                    Optional<User> existingUser = userService.findByGstNumber(request.getNewGstNumber());
                    if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                        response.put("success", false);
                        response.put("error", "GST number already exists");
                        return ResponseEntity.badRequest().body(response);
                    }
                }
                user.setGstNumber(request.getNewGstNumber());
            }
            
            // Save updated user
            User updatedUser = userService.save(user);
            
            // Build response
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            
            // Include updated user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", updatedUser.getId());
            userData.put("firstName", updatedUser.getFirstName());
            userData.put("lastName", updatedUser.getLastName());
            userData.put("mobile", updatedUser.getMobile());
            userData.put("gstNumber", updatedUser.getGstNumber());
//            userData.put("isApproved", updatedUser.getIsApproved());
//            userData.put("registrationDate", updatedUser.getRegistrationDate());
            userData.put("createdAt", updatedUser.getCreatedAt());
            
            response.put("user", userData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to update profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
//    @PostMapping("/verify-otp")
//    public ResponseEntity<Map<String, Object>> verifyOTP(@RequestBody Map<String, String> request) {
//        Map<String, Object> resp = new HashMap<>();
//        
//        String gstNumber = request.get("gstNumber");
//        String mobile = request.get("mobile");
//        String otp = request.get("otp");
//        
//        try {
//            User user = null;
//            
//            if (gstNumber != null && !gstNumber.isEmpty()) {
//                user = userRepository.findByGstNumber(gstNumber.toUpperCase()).orElse(null);
//            } else if (mobile != null && !mobile.isEmpty()) {
//                user = userRepository.findByMobile(mobile).orElse(null);
//            }
//            
//            if (user == null) {
//                resp.put("success", false);
//                resp.put("error", "User not found");
//                return ResponseEntity.badRequest().body(resp);
//            }
//            
//            // TODO: Implement actual OTP verification logic here
//            // For now, accept any 6-digit OTP for demo purposes
//            if (otp != null && otp.length() == 6) {
//                // Update user status to APPROVED after OTP verification
//                user.setStatus(Status.APPROVED);
//                userRepository.save(user);
//                
//                resp.put("success", true);
//                resp.put("message", "OTP verified successfully");
//                resp.put("user", Map.of(
//                    "id", user.getId(),
//                    "firstName", user.getFirstName(),
//                    "lastName", user.getLastName(),
//                    "gstNumber", user.getGstNumber(),
//                    "mobile", user.getMobile()
//                ));
//            } else {
//                resp.put("success", false);
//                resp.put("error", "Invalid OTP");
//            }
//            
//            return ResponseEntity.ok(resp);
//            
//        } catch (Exception e) {
//            resp.put("success", false);
//            resp.put("error", "OTP verification failed");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
//        }
//    }
    
    @PutMapping("/users/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(Status.APPROVED);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User approved"));
    }

    @PutMapping("/users/{id}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(Status.REJECTED);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User rejected"));
    }
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request) {

        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = optionalProduct.get();

        // Update fields (only these three)
        product.setName(request.getNameJson());
        product.setDescription(request.getDescriptionJson());
        product.setCategory(request.getCategory());

        Product updated = productRepository.save(product);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/products/{id}/disable")
    public ResponseEntity<Product> disableProduct(@PathVariable Long id) {
        return productRepository.findById(id)
            .map(product -> {
                product.setDisabled(true);
                Product saved = productRepository.save(product);
                return ResponseEntity.ok(saved);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/products/{id}/enable")
    public ResponseEntity<Product> enableProduct(@PathVariable Long id) {
        return productRepository.findById(id)
            .map(product -> {
                product.setDisabled(false);
                Product saved = productRepository.save(product);
                return ResponseEntity.ok(saved);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }


    
    @GetMapping("/users/pending")
    public List<User> getPendingUsers() {
        return userRepository.findByStatus(Status.PENDING);
    }
    
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PutMapping(value = "/greetings", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Greeting saveGreeting(
            @RequestParam("greeting") String greetingText,
            @RequestParam("image") MultipartFile imageFile
    ) throws Exception {
        Greeting greeting = new Greeting();
        greeting.setGreeting(greetingText);
        greeting.setCreatedAt(LocalDateTime.now());
        greeting.setImage(imageFile.getBytes());
        return greetingRepository.save(greeting);
    }
    
    @PutMapping("/products/{id}/price")
    public Product updateProductPrice(@PathVariable Long id, @RequestBody Map<String, Double> body) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        Double newPrice = body.get("price");
        product.setPrice(newPrice);
        productRepository.save(product);


        ProductPriceHistory history = new ProductPriceHistory();
        history.setProduct(product);
        history.setPrice(newPrice);
        history.setChangedAt(LocalDateTime.now());
        priceHistoryRepository.save(history);

        return product;
    }

    @GetMapping("/greetings")
    public List<Greeting> getAllGreetings() {
        return greetingRepository.findAll();
    }

    @GetMapping("/greetings/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Greeting greeting = greetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg") // or "image/png"
                .body(greeting.getImage());
    }
    
    @GetMapping("/products/{id}/price-history")
    public List<ProductPriceHistory> getProductPriceHistory(@PathVariable Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        return priceHistoryRepository.findByProductOrderByChangedAtAsc(product);
    }

    // -------------------- PRODUCT APIs --------------------


    @PostMapping("/products")
    public Product createProduct(@RequestBody CreateProductRequest request) {
        Product product = new Product();
        product.setCategory(request.getCategory());
        product.setNameMultilingual(request.getName());
        product.setDescriptionMultilingual(request.getDescription());
        product.setPrice(request.getPrice());
        
        return productRepository.save(product);
    }



    @GetMapping("/products")
    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductResponseDTO> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (Product p : products) {
            ProductResponseDTO dto = new ProductResponseDTO();
            dto.setId(p.getId());
            dto.setCategory(p.getCategory());
            dto.setPrice(p.getPrice());
            dto.setDisabled(p.isDisabled());   // <-- send status

            try {
                dto.setName(objectMapper.readValue(p.getName(), Map.class));
                dto.setDescription(objectMapper.readValue(p.getDescription(), Map.class));
            } catch (Exception e) {
                // handle if needed
            }

            List<Long> imageIds = p.getImages().stream()
                .map(ProductImage::getId)
                .collect(Collectors.toList());
            dto.setImageIds(imageIds);

            result.add(dto);
        }

        return result;
    }


    @PostMapping(value = "/products/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("image") MultipartFile imageFile
    ) throws Exception {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductImage productImage = new ProductImage();
        productImage.setImage(imageFile.getBytes());
        productImage.setProduct(product);

        productImage = productImageRepository.save(productImage);

       
        product.getImages().add(productImage);
        productRepository.save(product);

        Map<String, Object> response = new HashMap<>();
        response.put("imageId", productImage.getId());
        return response;
    }


    @GetMapping("/products/image/{imageId}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long imageId) {
        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(productImage.getImage());
    }
    @GetMapping("/products/{productId}/images")
    public List<Long> getProductImageIds(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        return product.getImages().stream()
            .map(ProductImage::getId)
            .toList();
    }


    @DeleteMapping("/products/image/{imageId}")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long imageId) {
        productImageRepository.deleteById(imageId);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/delete/all/products")
    public ResponseEntity<Void> deleteAllProducts() {
        productRepository.deleteAll();
        return ResponseEntity.noContent().build(); // 204
    }
    
    @DeleteMapping("/products/{id}")
    @Transactional
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            
          
            priceHistoryRepository.deleteByProduct(product);
            
          
            productRepository.deleteById(id);
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Product and associated data deleted successfully",
                "deletedProductId", id
            ));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "Product not found",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", "Failed to delete product: " + e.getMessage()
            ));
        }
    }
    @PostMapping("/user-sessions")
    public Map<String, Object> saveSessionData(@RequestBody Map<String, Object> sessionData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract data from request
            String userId = (String) sessionData.get("userId");
            Map<String, Object> userDetails = (Map<String, Object>) sessionData.get("userDetails");
            String sessionStartStr = (String) sessionData.get("sessionStart");
            String sessionEndStr = (String) sessionData.get("sessionEnd");
            Integer sessionDuration = (Integer) sessionData.get("sessionDuration");
            Integer totalTimeSpent = (Integer) sessionData.get("totalTimeSpent");
            Map<String, Object> deviceInfo = (Map<String, Object>) sessionData.get("deviceInfo");
            

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            LocalDateTime sessionStart = LocalDateTime.parse(sessionStartStr.replace("Z", ""));
            LocalDateTime sessionEnd = LocalDateTime.parse(sessionEndStr.replace("Z", ""));
            

            String userEmail = userDetails != null ? (String) userDetails.get("email") : null;
            String userMobile = userDetails != null ? (String) userDetails.get("mobile") : null;
            String userName = userDetails != null ? (String) userDetails.get("name") : null;
            if (userName == null && userEmail != null) userName = userEmail;
            if (userName == null) userName = "Unknown";
            String devicePlatform = deviceInfo != null ? (String) deviceInfo.get("platform") : "react-native";
            UserSession session = new UserSession();
            session.setUserId(userId);
            session.setUserEmail(userEmail);
            session.setUserMobile(userMobile);
            session.setUserName(userName);
            session.setSessionStart(sessionStart);
            session.setSessionEnd(sessionEnd);
            session.setSessionDuration(sessionDuration);
            session.setTotalTimeSpent(totalTimeSpent);
            session.setDevicePlatform(devicePlatform);
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            
            userSessionRepository.save(session);
            updateUserSessionSummary(userId, userEmail, userMobile, userName, totalTimeSpent, sessionEnd, sessionStart);
            
            response.put("success", true);
            response.put("message", "Session data saved successfully");
            response.put("sessionId", userId);
            response.put("totalTimeSpent", totalTimeSpent);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "Failed to save session data: " + e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/admin/user-sessions")
    public Map<String, Object> getAllUserSessions() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<UserSession> sessions = userSessionRepository.findAll();
            List<Map<String, Object>> sessionList = new ArrayList<>();
            
            for (UserSession session : sessions) {
                Map<String, Object> sessionMap = new HashMap<>();
                sessionMap.put("userId", session.getUserId());
                sessionMap.put("userEmail", session.getUserEmail());
                sessionMap.put("userMobile", session.getUserMobile());
                sessionMap.put("userName", session.getUserName());
                sessionMap.put("sessionStart", session.getSessionStart());
                sessionMap.put("sessionEnd", session.getSessionEnd());
                sessionMap.put("sessionDuration", session.getSessionDuration());
                sessionMap.put("totalTimeSpent", session.getTotalTimeSpent());
                sessionMap.put("devicePlatform", session.getDevicePlatform());
                sessionMap.put("createdAt", session.getCreatedAt());
                sessionList.add(sessionMap);
            }
            
            response.put("success", true);
            response.put("sessions", sessionList);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/admin/user-summary")
    public Map<String, Object> getAllUserSummaries() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<UserSessionSummary> summaries = userSessionSummaryRepository.findAllOrderByTotalTimeSpentDesc();
            List<Map<String, Object>> summaryList = new ArrayList<>();
            
            for (UserSessionSummary summary : summaries) {
                Map<String, Object> summaryMap = new HashMap<>();
                summaryMap.put("userId", summary.getUserId());
                summaryMap.put("userGstNumber", summary.getUserGstNumber());
                summaryMap.put("userMobile", summary.getUserMobile());
                summaryMap.put("userName", summary.getUserName());
                summaryMap.put("totalSessions", summary.getTotalSessions());
                summaryMap.put("totalTimeSpent", summary.getTotalTimeSpent());
                summaryMap.put("lastSessionDate", summary.getLastSessionDate());
                summaryMap.put("firstSessionDate", summary.getFirstSessionDate());
                summaryMap.put("createdAt", summary.getCreatedAt());
                summaryMap.put("updatedAt", summary.getUpdatedAt());
                summaryMap.put("totalTimeFormatted", formatTime(summary.getTotalTimeSpent()));
                summaryMap.put("lastSessionFormatted", summary.getLastSessionDate() != null ? 
                    summary.getLastSessionDate().toString() : "Never");
                summaryMap.put("firstSessionFormatted", summary.getFirstSessionDate() != null ? 
                    summary.getFirstSessionDate().toString() : "Never");
                
                summaryList.add(summaryMap);
            }
            
            response.put("success", true);
            response.put("users", summaryList);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/admin/user/{userId}")
    public Map<String, Object> getUserSessions(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<UserSession> sessions = userSessionRepository.findRecentSessionsByUserId(userId, 100);
            List<Map<String, Object>> sessionList = new ArrayList<>();
            
            for (UserSession session : sessions) {
                Map<String, Object> sessionMap = new HashMap<>();
                sessionMap.put("sessionStart", session.getSessionStart());
                sessionMap.put("sessionEnd", session.getSessionEnd());
                sessionMap.put("sessionDuration", session.getSessionDuration());
                sessionMap.put("totalTimeSpent", session.getTotalTimeSpent());
                sessionMap.put("devicePlatform", session.getDevicePlatform());
                sessionMap.put("createdAt", session.getCreatedAt());
                sessionMap.put("sessionDurationFormatted", formatTime(session.getSessionDuration()));
                sessionMap.put("totalTimeFormatted", formatTime(session.getTotalTimeSpent()));
                sessionMap.put("sessionStartFormatted", session.getSessionStart().toString());
                sessionMap.put("sessionEndFormatted", session.getSessionEnd().toString());
                
                sessionList.add(sessionMap);
            }
            
            response.put("success", true);
            response.put("sessions", sessionList);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/admin/dashboard-stats")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long totalUsers = userSessionSummaryRepository.countTotalUsers();
            Long totalSessions = userSessionSummaryRepository.sumTotalSessions();
            Long totalTime = userSessionSummaryRepository.sumTotalTimeSpent();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers != null ? totalUsers : 0);
            stats.put("totalSessions", totalSessions != null ? totalSessions : 0);
            stats.put("totalTime", totalTime != null ? totalTime : 0);
            stats.put("totalTimeFormatted", formatTime(totalTime != null ? totalTime.intValue() : 0));
            
            response.put("success", true);
            response.put("stats", stats);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    private void updateUserSessionSummary(String userId, String userGstNumber, String userMobile, 
                                         String userName, Integer totalTimeSpent, 
                                         LocalDateTime lastSessionDate, LocalDateTime firstSessionDate) {
        
        Optional<UserSessionSummary> existingSummary = userSessionSummaryRepository.findByUserId(userId);
        
        if (existingSummary.isPresent()) {
            UserSessionSummary summary = existingSummary.get();
            summary.setTotalSessions(summary.getTotalSessions() + 1);
            summary.setTotalTimeSpent(totalTimeSpent);
            summary.setLastSessionDate(lastSessionDate);
            summary.setUpdatedAt(LocalDateTime.now());
            userSessionSummaryRepository.save(summary);
        } else {
            UserSessionSummary newSummary = new UserSessionSummary();
            newSummary.setUserId(userId);
            newSummary.setUserGstNumber(userGstNumber);
            newSummary.setUserMobile(userMobile);
            newSummary.setUserName(userName);
            newSummary.setTotalSessions(1);
            newSummary.setTotalTimeSpent(totalTimeSpent);
            newSummary.setLastSessionDate(lastSessionDate);
            newSummary.setFirstSessionDate(firstSessionDate);
            newSummary.setCreatedAt(LocalDateTime.now());
            newSummary.setUpdatedAt(LocalDateTime.now());
            userSessionSummaryRepository.save(newSummary);
        }
    }
    
    
    private String formatTime(Integer seconds) {
        if (seconds == null) return "0s";

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
    
 

//    // Add this field to store OTPs (in-memory storage - for production use Redis or database)
//    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
//    private final Map<String, Long> otpExpiry = new ConcurrentHashMap<>();
//    private static final long OTP_EXPIRY_TIME = 5 * 60 * 1000; // 5 minutes
//    private static final String DEFAULT_MOBILE = "9000022066";
//
//    // ==================== OTP ENDPOINTS FOR SIGNUP ====================
//
    @PostMapping("/request-otp")
    public ResponseEntity<Map<String, Object>> requestOtp(@RequestBody Map<String, String> request) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String mobile = request.get("mobile");
            String gstNumber = request.get("gstNumber");
            String type = Optional.ofNullable(request.get("type")).orElse("signup");

            if ((mobile == null || mobile.isEmpty()) && (gstNumber == null || gstNumber.isEmpty())) {
                resp.put("success", false);
                resp.put("error", "Mobile or GST number is required");
                return ResponseEntity.badRequest().body(resp);
            }

            // For forgot_password, ensure user exists
            if ("forgot_password".equalsIgnoreCase(type)) {
                User user = null;
                if (gstNumber != null && !gstNumber.isEmpty()) {
                    user = userRepository.findByGstNumber(gstNumber.toUpperCase()).orElse(null);
                } else if (mobile != null && !mobile.isEmpty()) {
                    user = userRepository.findByMobile(mobile).orElse(null);
                }
                if (user == null) {
                    resp.put("success", false);
                    resp.put("error", "User not found");
                    return ResponseEntity.badRequest().body(resp);
                }
                if (mobile == null || mobile.isEmpty()) {
                    mobile = user.getMobile();
                }
            }

            // Validate mobile number format
            if (mobile != null && !mobile.matches("^[0-9]{10}$")) {
                resp.put("success", false);
                resp.put("error", "Invalid mobile number format. Must be 10 digits.");
                return ResponseEntity.badRequest().body(resp);
            }

            // Generate 6-digit OTP
            String otp = String.format("%06d", new Random().nextInt(1_000_000));
            String key = buildOtpKey(mobile, gstNumber, type);
            otpStore.put(key, otp);
            otpExpiry.put(key, System.currentTimeMillis() + OTP_EXPIRY_TIME);

            // Send SMS
            String smsResponse = smsService.sendOtp(mobile, otp);
            System.out.println("SMS Response: " + smsResponse);

            resp.put("success", true);
            resp.put("message", "OTP sent successfully");
            
            // TEMPORARY: Return OTP for testing (REMOVE IN PRODUCTION)
            resp.put("otp", otp);
            resp.put("debug", "Check backend console for OTP: " + otp);
            
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("error", "Failed to send OTP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
//
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String gstNumber = request.get("gstNumber");
            String mobile = request.get("mobile");
            String otp = request.get("otp");
            String type = Optional.ofNullable(request.get("type")).orElse("signup");

            // Trim and validate OTP
            if (otp == null || otp.trim().isEmpty()) {
                resp.put("success", false);
                resp.put("error", "OTP is required");
                return ResponseEntity.badRequest().body(resp);
            }
            
            otp = otp.trim(); // Trim whitespace

            if ((mobile == null || mobile.isEmpty()) && (gstNumber == null || gstNumber.isEmpty())) {
                resp.put("success", false);
                resp.put("error", "Mobile or GST number is required");
                return ResponseEntity.badRequest().body(resp);
            }

            // Build key (same way as in request-otp)
            String key = buildOtpKey(mobile, gstNumber, type);
            
            System.out.println("========================================");
            System.out.println("OTP Verification Attempt");
            System.out.println("Mobile: " + mobile);
            System.out.println("GST: " + gstNumber);
            System.out.println("Type: " + type);
            System.out.println("Key: " + key);
            System.out.println("Entered OTP: " + otp);
            System.out.println("========================================");

            String storedOtp = otpStore.get(key);
            Long expiryTime = otpExpiry.get(key);

            if (storedOtp == null || expiryTime == null) {
                System.out.println("OTP not found in store. Key: " + key);
                System.out.println("Available keys: " + otpStore.keySet());
                resp.put("success", false);
                resp.put("error", "OTP not found or expired. Please request a new OTP.");
                return ResponseEntity.badRequest().body(resp);
            }

            if (System.currentTimeMillis() > expiryTime) {
                System.out.println("OTP expired. Stored time: " + expiryTime + ", Current: " + System.currentTimeMillis());
                otpStore.remove(key);
                otpExpiry.remove(key);
                resp.put("success", false);
                resp.put("error", "OTP has expired. Please request a new OTP.");
                return ResponseEntity.badRequest().body(resp);
            }

            System.out.println("Stored OTP: " + storedOtp);
            System.out.println("Entered OTP: " + otp);
            System.out.println("Match: " + storedOtp.equals(otp));

            // Compare OTP (trim both for safety)
            if (!storedOtp.trim().equals(otp.trim())) {
                System.out.println("❌ OTP mismatch!");
                // DON'T remove OTP on wrong attempt - let user try again
                // Only remove on success or expiry
                resp.put("success", false);
                resp.put("error", "Invalid OTP. Please try again.");
                return ResponseEntity.badRequest().body(resp);
            }

            // OTP is valid – NOW clean up (only on success)
            System.out.println("✅ OTP verified successfully!");
            otpStore.remove(key);
            otpExpiry.remove(key);

            // For signup: Just verify OTP (user doesn't exist yet - will be created by frontend)
            if ("signup".equalsIgnoreCase(type)) {
                resp.put("success", true);
                resp.put("message", "OTP verified successfully");
                return ResponseEntity.ok(resp);
            }

            // For forgot_password: just confirm verification; frontend will now call /reset-password
            if ("forgot_password".equalsIgnoreCase(type)) {
                resp.put("success", true);
                resp.put("message", "OTP verified successfully. You can now reset your password.");
                return ResponseEntity.ok(resp);
            }

            // default
            resp.put("success", true);
            resp.put("message", "OTP verified successfully");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("error", "OTP verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
//
//    // ==================== OTP ENDPOINTS FOR FORGOT PASSWORD ====================
//
//    @PostMapping("/send-otp-forgot-password")
//    public ResponseEntity<Map<String, Object>> sendOTPForForgotPassword(@RequestBody Map<String, String> request) {
//        Map<String, Object> resp = new HashMap<>();
//        
//        try {
//            String email = request.get("email");
//            String mobile = request.get("mobile");
//            
//            if (email == null || email.isEmpty()) {
//                resp.put("success", false);
//                resp.put("error", "Email is required");
//                return ResponseEntity.badRequest().body(resp);
//            }
//            
//            // Verify user exists
//            User user = userRepository.findByEmail(email).orElse(null);
//            if (user == null) {
//                resp.put("success", false);
//                resp.put("error", "User not found with this email");
//                return ResponseEntity.badRequest().body(resp);
//            }
//            
//            // Generate 6-digit OTP
//            Random random = new Random();
//            String otp = String.format("%06d", random.nextInt(999999));
//            
//            // Store OTP with expiry (key: email)
//            String key = "forgot_" + email;
//            otpStore.put(key, otp);
//            otpExpiry.put(key, System.currentTimeMillis() + OTP_EXPIRY_TIME);
//            
//            // TODO: Send OTP via SMS to default mobile number
//            // For now, just log it (remove in production)
//            String targetMobile = mobile != null ? mobile : DEFAULT_MOBILE;
//            System.out.println("OTP for password reset (" + email + ") to " + targetMobile + ": " + otp);
//            
//            resp.put("success", true);
//            resp.put("message", "OTP sent successfully to " + targetMobile);
//            // In production, don't send OTP in response
//            resp.put("otp", otp); // Remove this in production
//            
//            return ResponseEntity.ok(resp);
//            
//        } catch (Exception e) {
//            resp.put("success", false);
//            resp.put("error", "Failed to send OTP: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
//        }
//    }
//
//    @PostMapping("/verify-otp-forgot-password")
//    public ResponseEntity<Map<String, Object>> verifyOTPForForgotPassword(@RequestBody Map<String, String> request) {
//        Map<String, Object> resp = new HashMap<>();
//        
//        try {
//            String email = request.get("email");
//            String otp = request.get("otp");
//            
//            if (email == null || otp == null) {
//                resp.put("success", false);
//                resp.put("error", "Email and OTP are required");
//                return ResponseEntity.badRequest().body(resp);
//            }
//            
//            String key = "forgot_" + email;
//            String storedOTP = otpStore.get(key);
//            Long expiryTime = otpExpiry.get(key);
//            
//            // Check if OTP exists and not expired
//            if (storedOTP == null || expiryTime == null) {
//                resp.put("success", false);
//                resp.put("error", "OTP not found or expired. Please request a new OTP.");
//                return ResponseEntity.badRequest().body(resp);
//            }
//            
//            if (System.currentTimeMillis() > expiryTime) {
//                otpStore.remove(key);
//                otpExpiry.remove(key);
//                resp.put("success", false);
//                resp.put("error", "OTP has expired. Please request a new OTP.");
//                return ResponseEntity.badRequest().body(resp);
//            }
//            
//            // For testing: accept any 4+ digit code
//            // In production, use: if (!storedOTP.equals(otp))
//            if (otp.length() < 4) {
//                resp.put("success", false);
//                resp.put("error", "Invalid OTP");
//                return ResponseEntity.badRequest().body(resp);
//            }
//            
//            // Don't remove OTP yet - need it for password reset
//            // Mark as verified by storing a verification token
//            String verifyKey = "verified_" + email;
//            otpStore.put(verifyKey, "verified");
//            otpExpiry.put(verifyKey, System.currentTimeMillis() + OTP_EXPIRY_TIME);
//            
//            resp.put("success", true);
//            resp.put("message", "OTP verified successfully");
//            
//            return ResponseEntity.ok(resp);
//            
//        } catch (Exception e) {
//            resp.put("success", false);
//            resp.put("error", "Failed to verify OTP: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
//        }
//    }
//
//    @PostMapping("/reset-password-with-otp")
//    public ResponseEntity<Map<String, Object>> resetPasswordWithOTP(@RequestBody Map<String, String> request) {
//        Map<String, Object> resp = new HashMap<>();
//        
//        try {
//            String email = request.get("email");
//            String otp = request.get("otp");
//            String newPassword = request.get("newPassword");
//            
//            if (email == null || email.isEmpty()) {
//                resp.put("success", false);
//                resp.put("error", "Email is required");
//                return ResponseEntity.badRequest().body(resp);
//            }
//            
//            if (newPassword == null || newPassword.length() < 6) {
//                resp.put("success", false);
//                resp.put("error", "Password must be at least 6 characters");
//                return ResponseEntity.badRequest().body(resp);
//            }
//            
//            // Verify OTP was verified
//            String verifyKey = "verified_" + email;
//            String verified = otpStore.get(verifyKey);
//            Long expiryTime = otpExpiry.get(verifyKey);
//            
//            if (verified == null || expiryTime == null || System.currentTimeMillis() > expiryTime) {
//                resp.put("success", false);
//                resp.put("error", "OTP verification expired. Please start again.");
//                return ResponseEntity.badRequest().body(resp);
//            }
//            
//            // Find user
//            User user = userRepository.findByEmail(email).orElse(null);
//            if (user == null) {
//                resp.put("success", false);
//                resp.put("error", "User not found");
//                return ResponseEntity.badRequest().body(resp);
//            }
//            
//            // Update password
//            user.setPasswordHash(passwordEncoder.encode(newPassword));
//            userRepository.save(user);
//            
//            // Remove verification token
//            otpStore.remove(verifyKey);
//            otpExpiry.remove(verifyKey);
//            
//            resp.put("success", true);
//            resp.put("message", "Password reset successfully");
//            
//            return ResponseEntity.ok(resp);
//            
//        } catch (Exception e) {
//            resp.put("success", false);
//            resp.put("error", "Failed to reset password: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
//        }
//    }
    @PostMapping("/login-otp")
    public ResponseEntity<Map<String, Object>> loginWithOTP(@RequestBody Map<String, String> request) {
        Map<String, Object> resp = new HashMap<>();
        
        try {
            String mobile = request.get("mobile");
            String otp = request.get("otp");
            
            // Validate input
            if (mobile == null || mobile.trim().isEmpty()) {
                resp.put("success", false);
                resp.put("error", "Mobile number is required");
                return ResponseEntity.badRequest().body(resp);
            }
            
            if (otp == null || otp.trim().isEmpty()) {
                resp.put("success", false);
                resp.put("error", "OTP is required");
                return ResponseEntity.badRequest().body(resp);
            }
            
            mobile = mobile.trim();
            otp = otp.trim();
            
            // Validate mobile format
            if (!mobile.matches("^[0-9]{10}$")) {
                resp.put("success", false);
                resp.put("error", "Invalid mobile number format. Must be 10 digits.");
                return ResponseEntity.badRequest().body(resp);
            }
            
            // Build OTP key (same as your buildOtpKey method for login type)
            String key = "login_M_" + mobile;
            
            // Verify OTP
            String storedOtp = otpStore.get(key);
            Long expiryTime = otpExpiry.get(key);
            
            if (storedOtp == null || expiryTime == null) {
                System.out.println("OTP not found. Key: " + key);
                System.out.println("Available keys: " + otpStore.keySet());
                resp.put("success", false);
                resp.put("error", "OTP not found or expired. Please request a new OTP.");
                return ResponseEntity.badRequest().body(resp);
            }
            
            if (System.currentTimeMillis() > expiryTime) {
                otpStore.remove(key);
                otpExpiry.remove(key);
                resp.put("success", false);
                resp.put("error", "OTP has expired. Please request a new OTP.");
                return ResponseEntity.badRequest().body(resp);
            }
            
            if (!storedOtp.trim().equals(otp.trim())) {
                resp.put("success", false);
                resp.put("error", "Invalid OTP. Please try again.");
                return ResponseEntity.badRequest().body(resp);
            }
            
            // OTP verified - clean up
            otpStore.remove(key);
            otpExpiry.remove(key);
            
            // Find user by mobile
            Optional<User> userOpt = userRepository.findByMobile(mobile);
            if (!userOpt.isPresent()) {
                System.out.println("User not found for mobile: " + mobile);
                resp.put("success", false);
                resp.put("error", "User not found");
                return ResponseEntity.badRequest().body(resp);
            }
            
            User user = userOpt.get();
            System.out.println("User found: " + user.getMobile() + ", ID: " + user.getId());
            
            // Generate token (use your existing JWT service or simple token)
            String token = "authenticated";  // Replace with your JWT token generation
            // OR: String token = jwtTokenProvider.generateToken(user.getMobile());
            
            // Build user response - handle null values properly
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("mobile", user.getMobile() != null ? user.getMobile() : "");
            userData.put("gstNumber", user.getGstNumber() != null ? user.getGstNumber() : "");
            userData.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
            userData.put("lastName", user.getLastName() != null ? user.getLastName() : "");
            userData.put("name", (user.getFirstName() + " " + user.getLastName()).trim());
            userData.put("status", user.getStatus() != null ? user.getStatus().toString() : "PENDING");
            userData.put("isApproved", user.getStatus() != null && user.getStatus() == Status.APPROVED);
            userData.put("approved", user.getStatus() != null && user.getStatus() == Status.APPROVED);
            
            // Handle dates
            if (user.getCreatedAt() != null) {
                userData.put("registrationDate", user.getCreatedAt().toString());
                userData.put("createdAt", user.getCreatedAt().toString());
            } else {
                userData.put("registrationDate", "");
                userData.put("createdAt", "");
            }
            
            // Return success response
            resp.put("success", true);
            resp.put("token", token);
            resp.put("user", userData);
            
            System.out.println("Login-OTP successful for user: " + user.getMobile());
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
    
//
}