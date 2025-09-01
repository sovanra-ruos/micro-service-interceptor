package notification.com.gatewayservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderGatewayFilterFactory headerGatewayFilterFactory;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                  ReactiveClientRegistrationRepository repository) {

        http
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/login/**",
                                "/oauth2/**",
                                "/logout/**",
                                "/error/**",
                                "/actuator/**",
                                "/identity/api/v1/auth/register",
                                "/identity/api/v1/auth/login",
                                "/identity/api/v1/auth/forgot-password",
                                "/identity/api/v1/auth/health",
                                "/interceptor/api/v1/headers/health"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oAuth2LoginSpec -> {
                    oAuth2LoginSpec.authenticationSuccessHandler(
                            new RedirectServerAuthenticationSuccessHandler("/"));
                    oAuth2LoginSpec.authenticationFailureHandler(
                            new RedirectServerAuthenticationFailureHandler("/login?error=true"));
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwkSetUri("http://localhost:8080/oauth2/jwks"))
                )
                .oauth2Client(oauth2 -> {
                    // Additional OAuth2 client configurations if needed
                })
                .logout(logoutSpec -> logoutSpec
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(repository)))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
            ReactiveClientRegistrationRepository repository) {

        OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(repository);

        oidcLogoutSuccessHandler.setLogoutSuccessUrl(URI.create("/"));

        return oidcLogoutSuccessHandler;
    }
}