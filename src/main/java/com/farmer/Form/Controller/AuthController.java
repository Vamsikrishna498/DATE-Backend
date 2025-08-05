package com.farmer.Form.Controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.farmer.Form.DTO.LoginRequest;
import com.farmer.Form.DTO.ResetPasswordDTO;
import com.farmer.Form.DTO.UserDTO;
import com.farmer.Form.DTO.UserResponseDTO;
import com.farmer.Form.DTO.UserViewDTO;
import com.farmer.Form.DTO.UserRegistrationDTO;
import com.farmer.Form.Entity.User;
import com.farmer.Form.Service.CountryStateCityService;
import com.farmer.Form.Service.EmailService;
import com.farmer.Form.Service.OtpService;
import com.farmer.Form.Service.UserService;
import com.farmer.Form.security.JwtUtil;
import com.farmer.Form.exception.UserNotApprovedException;
import com.farmer.Form.Repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final CountryStateCityService countryService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // ✅ LOGIN
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword())
            );
            String token = jwtUtil.generateToken(authentication);
            // Get user details
            User user = userService.getUserByEmailOrPhone(request.getUserName());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", user.getRole().name()); // Add role to response
            response.put("message", "Login successful");
            response.put("forcePasswordChange", user.isForcePasswordChange());
            
            // Add user information directly to login response
            response.put("user", new HashMap<String, Object>() {{
                put("id", user.getId());
                put("name", user.getName());
                put("email", user.getEmail());
                put("phoneNumber", user.getPhoneNumber());
                put("role", user.getRole().name());
                put("status", user.getStatus().name());
            }});
            
            return ResponseEntity.ok(response);
        } catch (UserNotApprovedException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Your account is not yet approved by admin.");
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(error);
        }
    }

    // ✅ REGISTER
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody UserDTO userDTO) {
        userService.registerUser(userDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Registration successful - waiting for approval");
        return ResponseEntity.ok(response);
    }

    // ✅ REGISTER WITH ROLE (for Employee and Farmer registrations)
    @PostMapping("/register-with-role")
    public ResponseEntity<Map<String, String>> registerUserWithRole(@Valid @RequestBody UserDTO userDTO) {
        // Validate that only EMPLOYEE or FARMER roles are allowed for this endpoint
        String role = userDTO.getRole().toUpperCase();
        if (!role.equals("EMPLOYEE") && !role.equals("FARMER")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Only EMPLOYEE and FARMER roles are allowed for this registration endpoint");
            return ResponseEntity.badRequest().body(error);
        }
        
        userService.registerUserWithRole(userDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Registration successful - waiting for approval");
        return ResponseEntity.ok(response);
    }

    // ✅ Simplified registration for role-based registration
    @PostMapping("/register-simple")
    public ResponseEntity<Map<String, String>> registerUserSimple(@Valid @RequestBody UserRegistrationDTO userDTO) {
        // Validate that only EMPLOYEE or FARMER roles are allowed for this endpoint
        String role = userDTO.getRole().toUpperCase();
        if (!role.equals("EMPLOYEE") && !role.equals("FARMER")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Only EMPLOYEE and FARMER roles are allowed for this registration endpoint");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Convert UserRegistrationDTO to UserDTO
        UserDTO userDTOConverted = new UserDTO();
        userDTOConverted.setName(userDTO.getName());
        userDTOConverted.setEmail(userDTO.getEmail());
        userDTOConverted.setPhoneNumber(userDTO.getPhoneNumber());
        userDTOConverted.setDateOfBirth(userDTO.getDateOfBirth());
        userDTOConverted.setGender(userDTO.getGender());
        userDTOConverted.setRole(userDTO.getRole());
        
        userService.registerUserWithRole(userDTOConverted);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Registration successful - waiting for approval");
        return ResponseEntity.ok(response);
    }

    // ✅ Simple Captcha endpoint
    @GetMapping("/captcha")
    public ResponseEntity<Map<String, String>> generateCaptcha() {
        // Generate a simple 6-character captcha
        String captcha = generateSimpleCaptcha();
        Map<String, String> response = new HashMap<>();
        response.put("captcha", captcha);
        return ResponseEntity.ok(response);
    }

    private String generateSimpleCaptcha() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            captcha.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return captcha.toString();
    }

    // ✅ SEND OTP
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> request) {
        String emailOrPhone = request.get("emailOrPhone");
        if (emailOrPhone == null || emailOrPhone.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email or phone number is required.");
        }
        String otp = otpService.generateAndSendOtp(emailOrPhone.trim());
        emailService.sendOtpEmail(emailOrPhone.trim(),
                "Your OTP is: " + otp + ". It is valid for 10 minutes.");
        return ResponseEntity.ok("OTP sent successfully to your registered email or phone.");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        try {
            String otp = otpService.generateAndSendOtp(email);
            return ResponseEntity.ok(Map.of("message", "OTP re-sent successfully to " + email));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error resending OTP"));
        }
    }

    // ✅ VERIFY OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
        String emailOrPhone = request.get("emailOrPhone");
        String otp = request.get("otp");
        if (emailOrPhone == null || emailOrPhone.trim().isEmpty() ||
                otp == null || otp.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email/Phone and OTP are required.");
        }
        boolean verified = otpService.verifyOtp(emailOrPhone.trim(), otp.trim());
        return verified
                ? ResponseEntity.ok("OTP verified successfully.")
                : ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                                .body("Invalid or expired OTP.");
    }

    // ✅ FORGOT USER ID
    @PostMapping("/forgot-user-id")
    public ResponseEntity<String> forgotUserId(@RequestBody Map<String, String> request) {
        String emailOrPhone = request.get("emailOrPhone");
        if (emailOrPhone == null || emailOrPhone.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email or phone number is required.");
        }
        String result = userService.forgotUserId(emailOrPhone.trim());
        return ResponseEntity.ok(result);
    }

    // ✅ FORGOT PASSWORD
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String emailOrPhone = request.get("emailOrPhone");
        if (emailOrPhone == null || emailOrPhone.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email or phone number is required.");
        }
        String otp = otpService.generateAndSendOtp(emailOrPhone.trim());
        emailService.sendOtpEmail(emailOrPhone.trim(),
                "Your password reset OTP is: " + otp + ". It is valid for 10 minutes.");
        return ResponseEntity.ok("Password reset OTP sent successfully.");
    }

    // ✅ RESET PASSWORD WITHOUT OTP
    @PostMapping("/reset-password/confirm")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {
        try {
            boolean success = userService.resetPasswordWithoutOtp(
                    request.getEmailOrPhone().trim(),
                    request.getNewPassword().trim(),
                    request.getConfirmPassword().trim()
            );
            return success
                    ? ResponseEntity.ok("Password changed successfully.")
                    : ResponseEntity.status(500).body("Password change failed.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("An error occurred: " + ex.getMessage());
        }
    }

    // 🌐 Location APIs
    @GetMapping("/countries")
    public ResponseEntity<String> getCountries() {
        return ResponseEntity.ok(countryService.getCountries());
    }

    @GetMapping("/states/{countryCode}")
    public ResponseEntity<String> getStates(@PathVariable String countryCode) {
        return ResponseEntity.ok(countryService.getStates(countryCode));
    }

    @GetMapping("/districts/{countryCode}/{stateCode}")
    public ResponseEntity<String> getDistricts(@PathVariable String countryCode,
                                               @PathVariable String stateCode) {
        return ResponseEntity.ok(countryService.getDistricts(countryCode, stateCode));
    }

    // ✅ Get all users (unfiltered)
    @GetMapping("/users/all")
    public ResponseEntity<List<UserViewDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ✅ Get users by role
    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<UserViewDTO>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    // ✅ Get users by status
    @GetMapping("/users/status/{status}")
    public ResponseEntity<List<UserViewDTO>> getUsersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(userService.getUsersByStatus(status));
    }

    // ✅ Get users by role and status
    @GetMapping("/users")
    public ResponseEntity<List<UserViewDTO>> getUsersByRoleAndStatus(
            @RequestParam String role,
            @RequestParam String status) {
        return ResponseEntity.ok(userService.getUsersByRoleAndStatus(role, status));
    }

    // ✅ Get individual user by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<UserViewDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ✅ Get current user profile
    @GetMapping("/users/profile")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userService.getUserByEmailOrPhone(userEmail);
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("name", user.getName());
            profile.put("email", user.getEmail());
            profile.put("phoneNumber", user.getPhoneNumber());
            profile.put("role", user.getRole().name());
            profile.put("status", user.getStatus().name());
            profile.put("forcePasswordChange", user.isForcePasswordChange());
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Error fetching user profile: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ✅ Update user status
    @PutMapping("/users/{id}/status")
    public ResponseEntity<String> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String newStatus = request.get("status");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Status is required");
        }
        userService.updateUserStatus(id, newStatus.trim());
        return ResponseEntity.ok("Status updated successfully");
    }

    // ✅ Approve user and assign role (Super Admin)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/users/{id}/approve")
    public ResponseEntity<String> approveAndAssignRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String role = request.get("role");
        if (role == null || role.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Role is required");
        }
        try {
            userService.approveAndAssignRole(id, role.trim());
            return ResponseEntity.ok("User approved and role assigned successfully. Credentials sent to user email.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ✅ Change password for authenticated user
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");
            
            if (currentPassword == null || newPassword == null || confirmPassword == null) {
                return ResponseEntity.badRequest().body("Current password, new password, and confirm password are required");
            }
            
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body("New password and confirm password do not match");
            }
            
            String userEmail = authentication.getName();
            User user = userService.getUserByEmailOrPhone(userEmail);
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }
            
            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setForcePasswordChange(false);
            userRepository.save(user);
            
            return ResponseEntity.ok("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error changing password: " + e.getMessage());
        }
    }

    // 🔧 Test endpoint
    @GetMapping("/test")
    public String test() {
        return "This is a test endpoint.";
    }

    // 🔧 Test login endpoint for debugging
    @PostMapping("/test-login")
    public ResponseEntity<Map<String, Object>> testLogin(@RequestBody Map<String, String> request) {
        try {
            String userName = request.get("userName");
            String password = request.get("password");
            
            if (userName == null || password == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "userName and password are required");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Try to authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userName, password)
            );
            
            // Get user details
            User user = userService.getUserByEmailOrPhone(userName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test login successful");
            response.put("userName", userName);
            response.put("userRole", user.getRole().name());
            response.put("userName_display", user.getName());
            response.put("userEmail", user.getEmail());
            response.put("userStatus", user.getStatus().name());
            response.put("forcePasswordChange", user.isForcePasswordChange());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Test login failed: " + e.getMessage());
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(error);
        }
    }

    // 🔧 Test registration endpoint
    @GetMapping("/test-registration")
    public String testRegistration() {
        return "Registration endpoint is accessible.";
    }
}
