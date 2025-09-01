package notification.com.gatewayservice.controller;


import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class GatewayController {

    @GetMapping("/")
    public Mono<Map<String, Object>> home(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Gateway Service is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("authenticated", authentication != null);

        if (authentication != null) {
            response.put("user", authentication.getName());
        }

        return Mono.just(response);
    }

    @GetMapping("/user")
    public Mono<Map<String, Object>> user(Authentication authentication,
                                          @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {
        Map<String, Object> response = new HashMap<>();

        if (authentication != null) {
            response.put("user", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
            response.put("client", authorizedClient.getClientRegistration().getClientId());

            if (authorizedClient.getAccessToken() != null) {
                response.put("accessToken", authorizedClient.getAccessToken().getTokenValue());
            }
        }

        return Mono.just(response);
    }

    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "gateway-service");
        response.put("timestamp", LocalDateTime.now());

        return Mono.just(response);
    }
}