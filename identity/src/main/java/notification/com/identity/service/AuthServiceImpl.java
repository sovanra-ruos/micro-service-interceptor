package notification.com.identity.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.com.identity.domain.Passcode;
import notification.com.identity.domain.User;
import notification.com.identity.dto.*;
import notification.com.identity.dto.user.UserCreateRequest;
import notification.com.identity.dto.user.UserResponse;
import notification.com.identity.mapper.UserMapper;
import notification.com.identity.repository.PasscodeRepository;
import notification.com.identity.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasscodeRepository passcodeRepository;
    private final PasscodeService passcodeService;
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest registerRequest) {
        log.info("Registering new user: {}", registerRequest.username());

        UserCreateRequest userCreateRequest = userMapper.mapRegisterRequestToUserCreationRequest(registerRequest);
        userService.checkForPasswords(registerRequest.password(), registerRequest.confirmedPassword());
        userService.checkTermsAndConditions(registerRequest.acceptTerms());
        userService.createNewUser(userCreateRequest);

        return userService.findByUsername(registerRequest.username());
    }

    @Override
    public UserResponse findMe(Authentication authentication) {
        isNotAuthenticated(authentication);
        log.info("Finding user info for: {}", authentication.getName());

        return userService.findByUsername(authentication.getName());
    }

    @Override
    @Transactional
    public void changePassword(Authentication authentication, ChangePasswordRequest changePasswordRequest) {
        log.info("Changing password for user: {}", authentication.getName());

        userService.isNotAuthenticated(authentication);
        userService.checkConfirmPasswords(
                changePasswordRequest.password(),
                changePasswordRequest.confirmedPassword()
        );
        userService.checkForOldPassword(authentication.getName(), changePasswordRequest.oldPassword());

        User user = userRepository.findByUsernameAndIsEnabledTrue(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User has not been found"));

        user.setPassword(passwordEncoder.encode(changePasswordRequest.password()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", authentication.getName());
    }

    @Override
    public void isNotAuthenticated(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication token is required");
        }
    }

    @Override
    @Transactional
    public UserResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.username());

        User user = userRepository.findByUsernameAndIsEnabledTrue(loginRequest.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            log.warn("Invalid password attempt for user: {}", loginRequest.username());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        log.info("Login successful for user: {}", loginRequest.username());
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        log.info("Processing forgot password request for: {}", forgotPasswordRequest.username());

        User user = userRepository.findByUsernameAndIsEnabledTrue(forgotPasswordRequest.username())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found or not enabled"
                ));

        // Clean up any existing passcodes for security
        passcodeRepository.deleteByUser(user);

        // Generate and send new passcode
        passcodeService.generate(user);

        log.info("Password reset token generated for user: {}", forgotPasswordRequest.username());
    }

    @Override
    @Transactional
    public void changeForgotPassword(ChangeForgotPasswordRequest request) {
        log.info("Processing forgot password change for user: {}", request.username());

        // Find and validate user
        User user = userRepository.findByUsernameAndIsEnabledTrue(request.username())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found or not enabled"
                ));

        // Find and validate passcode token
        Passcode passcode = passcodeRepository.findByToken(request.token())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invalid or expired verification token"
                ));

        // Validate token belongs to user
        if (!passcode.getUser().getId().equals(user.getId())) {
            log.warn("Token mismatch for user: {}", request.username());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid verification token"
            );
        }

        // Validate token has been verified
        if (Boolean.FALSE.equals(passcode.getIsValidated())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Token has not been validated. Please verify your token first"
            );
        }

        // Validate password confirmation
        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match"
            );
        }

        // Check if token is still valid (not expired)
        if (!passcodeService.isExpired(passcode)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Verification token has expired"
            );
        }

        // Update password and cleanup
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        // Clean up used passcode for security
        passcodeRepository.deleteByUser(user);

        log.info("Password reset completed successfully for user: {}", request.username());
    }
}
