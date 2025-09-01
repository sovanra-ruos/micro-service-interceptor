package notification.com.helperservice.feature.header.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.com.helperservice.feature.header.dto.EnrichmentRequest;
import notification.com.helperservice.feature.header.service.HeaderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1/headers")
@RequiredArgsConstructor
@Slf4j
public class HeaderController {

    private final HeaderService headerService;

    @GetMapping("/inspect")
    public ResponseEntity<Map<String, Object>> inspectHeaders(
            @RequestParam(value = "service", defaultValue = "unknown") String serviceName,
            HttpServletRequest request) {

        String correlationId = getHeaderValue(request, "X-Correlation-ID");
        log.info("Header inspection request received - Correlation-ID: {}, Target-Service: {}",
                correlationId, serviceName);

        Map<String, String> headers = extractAllHeaders(request);
        Map<String, Object> processing = headerService.processHeaders(headers);
        Map<String, Object> summary = headerService.createHeaderSummary(headers);

        // Show what enrichment would look like for this service
        Map<String, String> enrichedHeaders = headerService.enrichHeaders(headers, serviceName);

        Map<String, Object> response = createResponse("Headers inspected successfully",
                correlationId, request.getHeader("X-Request-ID"));
        response.put("targetService", serviceName);
        response.put("originalHeaders", headers);
        response.put("processing", processing);
        response.put("summary", summary);
        response.put("enrichmentPreview", enrichedHeaders);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/enrich")
    public ResponseEntity<Map<String, Object>> enrichHeaders(
            @RequestBody EnrichmentRequest enrichmentRequest,
            HttpServletRequest request) {

        String correlationId = getHeaderValue(request, "X-Correlation-ID");
        String serviceName = Optional.ofNullable(enrichmentRequest.getTargetService()).orElse("unknown");

        log.info("Header enrichment request received - Correlation-ID: {}, Target-Service: {}",
                correlationId, serviceName);

        Map<String, String> requestHeaders = extractAllHeaders(request);
        Map<String, String> allHeaders = new HashMap<>(requestHeaders);

        if (enrichmentRequest.getHeaders() != null) {
            allHeaders.putAll(enrichmentRequest.getHeaders());
        }

        Map<String, String> enrichedHeaders = headerService.enrichHeaders(allHeaders, serviceName);

        Map<String, Object> response = createResponse("Headers enriched successfully",
                correlationId, request.getHeader("X-Request-ID"));
        response.put("targetService", serviceName);
        response.put("originalHeaders", enrichmentRequest.getHeaders());
        response.put("requestHeaders", requestHeaders);
        response.put("enrichedHeaders", enrichedHeaders);
        response.put("enrichmentSummary", headerService.createHeaderSummary(enrichedHeaders));
        response.put("enrichmentCount", enrichedHeaders.size() - allHeaders.size());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/enrich/{serviceName}")
    public ResponseEntity<Map<String, Object>> enrichHeadersForService(
            @PathVariable String serviceName,
            @RequestBody(required = false) Map<String, String> inputHeaders,
            HttpServletRequest request) {

        String correlationId = getHeaderValue(request, "X-Correlation-ID");
        log.info("Service-specific header enrichment - Correlation-ID: {}, Service: {}",
                correlationId, serviceName);

        Map<String, String> requestHeaders = extractAllHeaders(request);
        Map<String, String> allHeaders = new HashMap<>(requestHeaders);

        if (inputHeaders != null) {
            allHeaders.putAll(inputHeaders);
        }

        Map<String, String> enrichedHeaders = headerService.enrichHeaders(allHeaders, serviceName);

        Map<String, Object> response = createResponse(
                "Headers enriched for " + serviceName + " service",
                correlationId, request.getHeader("X-Request-ID"));
        response.put("targetService", serviceName);
        response.put("originalHeaders", inputHeaders);
        response.put("requestHeaders", requestHeaders);
        response.put("enrichedHeaders", enrichedHeaders);
        response.put("serviceSpecificEnrichment",
                headerService.getServiceSpecificHeaders(serviceName, allHeaders));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getSupportedServices(HttpServletRequest request) {
        String correlationId = getHeaderValue(request, "X-Correlation-ID");

        Set<String> supportedServices = Set.of(
                "product-service", "order-service",
                "notification-service", "user-service");

        Map<String, Object> response = createResponse("Supported services retrieved",
                correlationId, request.getHeader("X-Request-ID"));
        response.put("supportedServices", supportedServices);
        response.put("totalServices", supportedServices.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health(HttpServletRequest request) {
        Map<String, Object> response = createResponse("Helper service is healthy",
                getHeaderValue(request, "X-Correlation-ID"), getHeaderValue(request, "X-Request-ID"));
        response.put("status", "UP");
        response.put("service", "helper-service");
        response.put("role", "header-interceptor-proxy");

        return ResponseEntity.ok(response);
    }

    // Helper methods
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

    private Map<String, Object> createResponse(String message, String correlationId, String requestId) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("timestamp", Instant.now().toString());
        response.put("correlationId", correlationId);
        response.put("requestId", requestId);
        response.put("service", "helper-service");
        return response;
    }
}



