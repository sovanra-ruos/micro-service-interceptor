package notification.com.identity.dto.user;

import java.util.Set;

public record UserCreateRequest(
        String username,
        String email,
        String password,
        String confirmedPassword,
        String familyName,
        String givenName,
        String acceptTerms,
        Set<String> authorities
) {}
