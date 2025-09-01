package notification.com.gatewayservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class HeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<HeaderGatewayFilterFactory.Config> {

    public HeaderGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            final String correlationId = Optional.ofNullable(
                            exchange.getRequest().getHeaders().getFirst("X-Correlation-ID"))
                    .orElse(UUID.randomUUID().toString());

            final String requestId = UUID.randomUUID().toString();

            log.info("Gateway processing request - Correlation-ID: {}, Request-ID: {}, Path: {}",
                    correlationId, requestId, exchange.getRequest().getPath());

            return ReactiveSecurityContextHolder.getContext()
                    .cast(SecurityContext.class)
                    .map(SecurityContext::getAuthentication)
                    .flatMap(authentication -> {
                        ServerHttpRequest enhancedRequest = buildEnhancedRequest(
                                exchange.getRequest(), correlationId, requestId, authentication);

                        return chain.filter(exchange.mutate().request(enhancedRequest).build())
                                .doOnSuccess(unused -> log.info("Gateway request completed - Correlation-ID: {}, Request-ID: {}",
                                        correlationId, requestId))
                                .doOnError(throwable -> log.error("Gateway request failed - Correlation-ID: {}, Request-ID: {}, Error: {}",
                                        correlationId, requestId, throwable.getMessage()));
                    })
                    .switchIfEmpty(
                            Mono.defer(() -> {
                                log.warn("No authentication context found for request - Correlation-ID: {}", correlationId);

                                ServerHttpRequest enhancedRequest = buildEnhancedRequest(
                                        exchange.getRequest(), correlationId, requestId, null);

                                return chain.filter(exchange.mutate().request(enhancedRequest).build());
                            })
                    );
        };
    }

    private ServerHttpRequest buildEnhancedRequest(ServerHttpRequest originalRequest,
                                                   String correlationId,
                                                   String requestId,
                                                   Authentication authentication) {
        ServerHttpRequest.Builder requestBuilder = originalRequest.mutate();

        // Add standard headers
        requestBuilder
                .header("X-Correlation-ID", correlationId)
                .header("X-Request-ID", requestId)
                .header("X-Gateway-Timestamp", Instant.now().toString())
                .header("X-Gateway-Service", "gateway-service")
                .header("X-Gateway-Version", "1.0");

        // Extract user information from JWT if authenticated
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            addUserHeaders(requestBuilder, jwt);
            log.debug("Added user headers for authenticated request - Correlation-ID: {}", correlationId);
        } else {
            requestBuilder.header("X-Anonymous-Request", "true");
            log.debug("Added anonymous headers - Correlation-ID: {}", correlationId);
        }

        return requestBuilder.build();
    }

    private void addUserHeaders(ServerHttpRequest.Builder requestBuilder, Jwt jwt) {
        Optional.ofNullable(jwt.getClaimAsString("sub"))
                .ifPresent(username -> requestBuilder.header("X-Username", username));

        Optional.ofNullable(jwt.getClaimAsString("uuid"))
                .ifPresent(userUuid -> requestBuilder.header("X-User-UUID", userUuid));

        Optional.ofNullable(jwt.getClaimAsString("email"))
                .ifPresent(email -> requestBuilder.header("X-User-Email", email));

        // Add roles/authorities
        Collection<String> authorities = jwt.getClaimAsStringList("authorities");
        if (authorities != null && !authorities.isEmpty()) {
            requestBuilder.header("X-User-Authorities", String.join(",", authorities));
        }
    }

    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}


