package notification.com.identity.feature.service;

import notification.com.identity.domain.User;
import notification.com.identity.feature.dto.user.UserCreateRequest;
import notification.com.identity.feature.dto.user.UserPasswordResetResponse;
import notification.com.identity.feature.dto.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

public interface UserService {
    void createNewUser(UserCreateRequest userCreateRequest);
    void isNotAuthenticated(Authentication authentication);
    UserResponse getAuthenticatedUser(Authentication authentication);
    UserPasswordResetResponse resetPassword(String username);
    void enable(String username);
    void disable(String username);
    Page<UserResponse> findList(int pageNumber, int pageSize);
    UserResponse findByUsername(String username);
    void checkForPasswords(String password, String confirmPassword);
    void checkTermsAndConditions(String value);
    void existsByUsername(String username);
    void existsByEmail(String email);
    void checkConfirmPasswords(String password, String confirmPassword);
    void verifyEmail(User user);
    void checkForOldPassword(String username, String oldPassword);
}
