package notification.com.identity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.com.identity.dto.*;
import notification.com.identity.dto.user.UserResponse;
import notification.com.identity.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration request received for username: {}", registerRequest.username());

        UserResponse userResponse = authService.register(registerRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("data", userResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for username: {}", loginRequest.username());

        UserResponse userResponse = authService.login(loginRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("timestamp", LocalDateTime.now());
        response.put("data", userResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        log.info("Forgot password request received for username: {}", forgotPasswordRequest.username());

        authService.forgotPassword(forgotPasswordRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password reset token has been sent");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-forgot-password")
    public ResponseEntity<Map<String, Object>> changeForgotPassword(@Valid @RequestBody ChangeForgotPasswordRequest changeForgotPasswordRequest) {
        log.info("Change forgot password request received for username: {}", changeForgotPasswordRequest.username());

        authService.changeForgotPassword(changeForgotPasswordRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password has been reset successfully");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> changePassword(Authentication authentication,
                                                              @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        log.info("Change password request received for user: {}", authentication.getName());

        authService.changePassword(authentication, changePasswordRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_profile') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> findMe(Authentication authentication) {
        log.info("User profile request received for: {}", authentication.getName());

        // Log JWT details for debugging
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            log.debug("JWT token received with subject: {}", jwt.getSubject());
        }

        UserResponse userResponse = authService.findMe(authentication);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User profile retrieved successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("data", userResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "identity-auth-service");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
