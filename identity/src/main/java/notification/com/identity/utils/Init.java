package notification.com.identity.utils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.com.identity.domain.Administrator;
import notification.com.identity.domain.Authority;
import notification.com.identity.domain.User;
import notification.com.identity.domain.UserAuthority;
import notification.com.identity.feature.repository.AdministratorRepository;
import notification.com.identity.feature.repository.AuthorityRepository;
import notification.com.identity.feature.repository.JpaRegisteredClientRepository;
import notification.com.identity.feature.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class Init {

    private final JpaRegisteredClientRepository jpaRegisteredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final AdministratorRepository administratorRepository;

    @PostConstruct
    void initUserDetails() {
        if (userRepository.count() < 1) {
            log.info("Initializing default users and authorities...");

            // Authority initialization
            Authority user = createAuthority("USER");
            Authority system = createAuthority("SYSTEM");
            Authority admin = createAuthority("ADMIN");
            Authority editor = createAuthority("EDITOR");

            // Admin User initialization
            User adminUser = createAdminUser(user, admin);

            // Administrator entity
            Administrator administrator = new Administrator();
            administrator.setUser(adminUser);
            administratorRepository.save(administrator);

            log.info("Default users and authorities initialized successfully");
        }
    }

    @PostConstruct
    void initOAuth2() {
        RegisteredClient existingClient = jpaRegisteredClientRepository.findByClientId("devops");

        if (existingClient == null) {
            log.info("Initializing OAuth2 clients...");

            TokenSettings tokenSettings = TokenSettings.builder()
                    .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                    .accessTokenTimeToLive(Duration.ofDays(1))
                    .refreshTokenTimeToLive(Duration.ofDays(7))
                    .idTokenSignatureAlgorithm(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256)
                    .reuseRefreshTokens(false)
                    .build();

            // Disable PKCE as requested
            ClientSettings clientSettings = ClientSettings.builder()
                    .requireProofKey(false) // PKCE disabled
                    .requireAuthorizationConsent(false)
                    .build();

            RegisteredClient webClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("devops")
                    .clientSecret(passwordEncoder.encode("Qwerty@2024"))
                    .scopes(scopes -> {
                        scopes.add(OidcScopes.OPENID);
                        scopes.add(OidcScopes.PROFILE);
                        scopes.add(OidcScopes.EMAIL);
                        scopes.add("read");
                        scopes.add("write");
                    })
                    .redirectUris(uris -> {
                        uris.add("http://localhost:8081/login/oauth2/code/devops");
                        uris.add("http://gateway-service/login/oauth2/code/devops");
                    })
                    .postLogoutRedirectUris(uris -> {
                        uris.add("http://localhost:8081");
                        uris.add("http://gateway-service");
                    })
                    .clientAuthenticationMethods(methods -> {
                        methods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                        methods.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
                    })
                    .authorizationGrantTypes(grantTypes -> {
                        grantTypes.add(AuthorizationGrantType.AUTHORIZATION_CODE);
                        grantTypes.add(AuthorizationGrantType.REFRESH_TOKEN);
                        grantTypes.add(AuthorizationGrantType.CLIENT_CREDENTIALS);
                    })
                    .clientSettings(clientSettings)
                    .tokenSettings(tokenSettings)
                    .build();

            jpaRegisteredClientRepository.save(webClient);
            log.info("OAuth2 client 'devops' initialized successfully");
        }
    }

    private Authority createAuthority(String name) {
        Authority authority = new Authority();
        authority.setName(name);
        return authorityRepository.save(authority);
    }

    private User createAdminUser(Authority userAuthority, Authority adminAuthority) {
        User adminUser = new User();
        adminUser.setUuid(UUID.randomUUID().toString());
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@devops.com");
        adminUser.setPassword(passwordEncoder.encode("Qwerty@2024"));
        adminUser.setFamilyName("System");
        adminUser.setGivenName("Administrator");
        adminUser.setProfileImage("avatar.png");
        adminUser.setEmailVerified(true);
        adminUser.setIsEnabled(true);
        adminUser.setCredentialsNonExpired(true);
        adminUser.setAccountNonLocked(true);
        adminUser.setAccountNonExpired(true);

        // Setup user authorities
        UserAuthority defaultUserAuthority = new UserAuthority();
        defaultUserAuthority.setUser(adminUser);
        defaultUserAuthority.setAuthority(userAuthority);

        UserAuthority adminUserAuthority = new UserAuthority();
        adminUserAuthority.setUser(adminUser);
        adminUserAuthority.setAuthority(adminAuthority);

        adminUser.setUserAuthorities(Set.of(defaultUserAuthority, adminUserAuthority));

        return userRepository.save(adminUser);
    }
}
