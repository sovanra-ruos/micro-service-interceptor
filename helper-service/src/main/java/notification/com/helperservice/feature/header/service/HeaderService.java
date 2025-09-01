package notification.com.helperservice.feature.header.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class HeaderService {

    public Map<String, Object> processHeaders(Map<String, String> incomingHeaders) {
        log.info("Processing headers count: {}", incomingHeaders.size());

        Map<String, Object> result = new HashMap<>();
        result.put("processedAt", Instant.now().toString());
        result.put("processedBy", "helper-service");
        result.put("headerCount", incomingHeaders.size());
        result.put("hasAuthorization", incomingHeaders.containsKey("Authorization"));
        result.put("hasCorrelationId", incomingHeaders.containsKey("X-Correlation-ID"));
        result.put("hasUserInfo", incomingHeaders.containsKey("X-Username"));
        result.put("isAuthenticated", !incomingHeaders.containsKey("X-Anonymous-Request"));

        return result;
    }

    public Map<String, String> enrichHeaders(Map<String, String> originalHeaders, String targetService) {
        Map<String, String> enrichedHeaders = new HashMap<>(originalHeaders);

        // Add enrichment metadata
        enrichedHeaders.put("X-Enriched", "true");
        enrichedHeaders.put("X-Enriched-At", Instant.now().toString());
        enrichedHeaders.put("X-Enriched-By", "helper-service");
        enrichedHeaders.put("X-Processing-Time", String.valueOf(System.currentTimeMillis()));
        enrichedHeaders.put("X-Target-Service", targetService);

        // Add user context if available
        if (originalHeaders.containsKey("X-Username")) {
            enrichedHeaders.put("X-User-Context", "authenticated");
        } else {
            enrichedHeaders.put("X-User-Context", "anonymous");
        }

        // Add request metadata
        enrichedHeaders.put("X-Request-Source", "gateway-via-interceptor");

        // Service-specific enrichment
        enrichedHeaders.putAll(getServiceSpecificHeaders(targetService, originalHeaders));

        log.debug("Headers enriched for {} - Original: {}, Enriched: {}",
                targetService, originalHeaders.size(), enrichedHeaders.size());

        return enrichedHeaders;
    }

    public Map<String, String> getServiceSpecificHeaders(String serviceName, Map<String, String> originalHeaders) {
        Map<String, String> serviceHeaders = new HashMap<>();

        switch (serviceName) {
            case "product-service":
                serviceHeaders.put("X-Service-Category", "catalog");
                serviceHeaders.put("X-Cache-Strategy", "aggressive");
                break;
            case "order-service":
                serviceHeaders.put("X-Service-Category", "transaction");
                serviceHeaders.put("X-Cache-Strategy", "minimal");
                serviceHeaders.put("X-Audit-Required", "true");
                break;
            case "notification-service":
                serviceHeaders.put("X-Service-Category", "communication");
                serviceHeaders.put("X-Priority", "normal");
                break;
            case "user-service":
                serviceHeaders.put("X-Service-Category", "identity");
                serviceHeaders.put("X-Data-Sensitivity", "high");
                break;
            default:
                serviceHeaders.put("X-Service-Category", "unknown");
        }

        return serviceHeaders;
    }

    public Map<String, Object> createHeaderSummary(Map<String, String> headers) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalHeaders", headers.size());
        summary.put("hasAuth", headers.containsKey("Authorization"));
        summary.put("correlationId", headers.get("X-Correlation-ID"));
        summary.put("requestId", headers.get("X-Request-ID"));
        summary.put("userInfo", extractUserSummary(headers));
        return summary;
    }

    private Map<String, String> extractUserSummary(Map<String, String> headers) {
        Map<String, String> userInfo = new HashMap<>();

        Optional.ofNullable(headers.get("X-Username"))
                .ifPresent(username -> userInfo.put("username", username));

        Optional.ofNullable(headers.get("X-User-UUID"))
                .ifPresent(uuid -> userInfo.put("uuid", uuid));

        Optional.ofNullable(headers.get("X-User-Email"))
                .ifPresent(email -> userInfo.put("email", email));

        return userInfo;
    }
}





