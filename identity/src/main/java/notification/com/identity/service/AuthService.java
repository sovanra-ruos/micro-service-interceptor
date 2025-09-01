package notification.com.identity.service;

import notification.com.identity.dto.*;
import notification.com.identity.dto.user.UserResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    UserResponse register(RegisterRequest registerRequest);
    UserResponse findMe(Authentication authentication);
    void isNotAuthenticated(Authentication authentication);
    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    UserResponse login(LoginRequest loginRequest);
    void changePassword(Authentication authentication, ChangePasswordRequest changePasswordRequest);
    void changeForgotPassword(ChangeForgotPasswordRequest changeForgotPasswordRequest);
}