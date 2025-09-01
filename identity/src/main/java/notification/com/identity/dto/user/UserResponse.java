package notification.com.identity.dto.user;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        String uuid,
        String username,
        String email,
        String familyName,
        String givenName,
        String profileImage,
        Boolean emailVerified,
        Boolean isEnabled,
        Set<String> authorities,
        LocalDateTime createdDate
) {}
