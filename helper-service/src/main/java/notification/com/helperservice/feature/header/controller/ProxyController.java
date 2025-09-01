package notification.com.helperservice.feature.header.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.com.helperservice.config.ServiceConfig;
import notification.com.helperservice.feature.header.service.HeaderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/proxy")
@RequiredArgsConstructor
@Slf4j
public class ProxyController {

    private final HeaderService headerService;
    private final WebClient.Builder webClientBuilder;
    private final ServiceConfig serviceConfig;

    @RequestMapping(value = "/{serviceName}/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<ResponseEntity<Object>> proxyToService(
            @PathVariable String serviceName,
            HttpServletRequest request,
            @RequestBody(required = false) Object body) {

        String correlationId = getHeaderValue(request, "X-Correlation-ID");
        String requestId = getHeaderValue(request, "X-Request-ID");

        log.info("Proxying request to {} service - Correlation-ID: {}, Request-ID: {}, Method: {}, Path: {}",
                serviceName, correlationId, requestId, request.getMethod(), request.getRequestURI());

        // Validate service configuration
        ServiceConfig.ServiceInfo serviceInfo = serviceConfig.getServiceInfo(serviceName);
        if (serviceInfo == null) {
            log.error("Unknown service: {} - Correlation-ID: {}", serviceName, correlationId);
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Unknown service: " + serviceName, correlationId, requestId)));
        }

        // Validate HTTP method
        if (!serviceInfo.getAllowedMethods().contains(request.getMethod())) {
            log.error("Method {} not allowed for service: {} - Correlation-ID: {}",
                    request.getMethod(), serviceName, correlationId);
            return Mono.just(ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body(createErrorResponse("Method not allowed", correlationId, requestId)));
        }

        // Extract and enrich headers
        Map<String, String> originalHeaders = extractAllHeaders(request);
        Map<String, String> enrichedHeaders = headerService.enrichHeaders(originalHeaders, serviceName);

        // Build target URL
        String targetUrl = buildTargetUrl(request, serviceInfo, serviceName);

        // Build and execute request
        return buildAndExecuteRequest(request, body, targetUrl, enrichedHeaders, correlationId, requestId, serviceName);
    }

    private String buildTargetUrl(HttpServletRequest request, ServiceConfig.ServiceInfo serviceInfo, String serviceName) {
        String targetPath = request.getRequestURI()
                .replace("/api/v1/proxy/" + serviceName, serviceInfo.getBasePath());

        String targetUrl = serviceInfo.getUrl() + targetPath;

        if (request.getQueryString() != null) {
            targetUrl += "?" + request.getQueryString();
        }

        return targetUrl;
    }

    private Mono<ResponseEntity<Object>> buildAndExecuteRequest(
            HttpServletRequest request,
            Object body,
            String targetUrl,
            Map<String, String> enrichedHeaders,
            String correlationId,
            String requestId,
            String serviceName) {

        WebClient.RequestBodySpec requestSpec = webClientBuilder.build()
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(targetUrl);

        // Add all enriched headers
        enrichedHeaders.forEach(requestSpec::header);

        // Add proxy-specific headers
        requestSpec
                .header("X-Via-Interceptor", "true")
                .header("X-Proxy-Service", "helper-service")
                .header("X-Target-Service", serviceName);

        // Add body if present for write operations
        if (body != null && isWriteOperation(request.getMethod())) {
            requestSpec.bodyValue(body);
        }

        ServiceConfig.ServiceInfo serviceInfo = serviceConfig.getServiceInfo(serviceName);
        Duration timeout = Duration.ofSeconds(serviceInfo.getTimeout());

        return requestSpec
                .retrieve()
                .toEntity(Object.class)
                .timeout(timeout)
                .doOnSuccess(response -> log.info("Proxy request completed - Service: {}, Correlation-ID: {}, Request-ID: {}, Status: {}",
                        serviceName, correlationId, requestId, response.getStatusCode()))
                .doOnError(error -> log.error("Proxy request failed - Service: {}, Correlation-ID: {}, Request-ID: {}, Error: {}",
                        serviceName, correlationId, requestId, error.getMessage()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(createErrorResponse("Proxy request failed to " + serviceName, correlationId, requestId)));
    }

    private boolean isWriteOperation(String method) {
        return Set.of("POST", "PUT", "PATCH").contains(method);
    }

    private Map<String, String> extractAllHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }

        return headers;
    }

    private String getHeaderValue(HttpServletRequest request, String headerName) {
        return Optional.ofNullable(request.getHeader(headerName))
                .orElse(UUID.randomUUID().toString());
    }

    private Map<String, Object> createErrorResponse(String message, String correlationId, String requestId) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("correlationId", correlationId);
        error.put("requestId", requestId);
        error.put("timestamp", Instant.now().toString());
        error.put("service", "helper-service");
        return error;
    }
}




