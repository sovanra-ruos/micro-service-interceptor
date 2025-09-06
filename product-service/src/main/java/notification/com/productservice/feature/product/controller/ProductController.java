package notification.com.productservice.feature.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.com.productservice.feature.product.repository.dto.ProductCreateRequest;
import notification.com.productservice.feature.product.repository.dto.ProductResponse;
import notification.com.productservice.feature.product.repository.dto.RequestContext;
import notification.com.productservice.feature.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('EDITOR')")
    public ResponseEntity<Map<String, Object>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            HttpServletRequest httpRequest) {

        RequestContext context = extractRequestContext(httpRequest);
        logRequestDetails(context, "CREATE_PRODUCT");

        try {
            String createdBy = context.getUsername() != null ? context.getUsername() : "system";
            ProductResponse productResponse = productService.createProduct(request, createdBy);

            Map<String, Object> response = createSuccessResponse(
                    "Product created successfully", productResponse, context);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Failed to create product - Correlation-ID: {}, Error: {}",
                    context.getCorrelationId(), e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create product", context, e));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProduct(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        RequestContext context = extractRequestContext(httpRequest);
        logRequestDetails(context, "GET_PRODUCT");

        try {
            ProductResponse productResponse = productService.getProductById(id);
            Map<String, Object> response = createSuccessResponse(
                    "Product retrieved successfully", productResponse, context);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get product {} - Correlation-ID: {}, Error: {}",
                    id, context.getCorrelationId(), e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Product not found", context, e));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {

        RequestContext context = extractRequestContext(httpRequest);
        logRequestDetails(context, "GET_ALL_PRODUCTS");

        try {
            Page<ProductResponse> products = productService.getAllProducts(page, size, sortBy, sortDir);
            Map<String, Object> response = createSuccessResponse(
                    "Products retrieved successfully", products, context);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get products - Correlation-ID: {}, Error: {}",
                    context.getCorrelationId(), e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve products", context, e));
        }
    }

    @GetMapping("/headers-demo")
    public ResponseEntity<Map<String, Object>> headersDemo(HttpServletRequest httpRequest) {
        RequestContext context = extractRequestContext(httpRequest);
        logRequestDetails(context, "HEADERS_DEMO");

        Map<String, Object> response = createSuccessResponse(
                "Headers demonstration", null, context);
        response.put("allHeaders", extractAllHeaders(httpRequest));
        response.put("userContext", createUserContext(context));
        response.put("routingInfo", createRoutingInfo(context));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health(HttpServletRequest httpRequest) {
        RequestContext context = extractRequestContext(httpRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "product-service");
        response.put("timestamp", Instant.now().toString());
        response.put("correlationId", context.getCorrelationId());
        response.put("requestId", context.getRequestId());
        response.put("viaInterceptor", context.isViaInterceptor());
        response.put("directAccess", context.isDirectAccess());

        return ResponseEntity.ok(response);
    }

    // Helper methods
    private RequestContext extractRequestContext(HttpServletRequest request) {
        return RequestContext.builder()
                .correlationId(getHeaderValue(request, "X-Correlation-ID"))
                .requestId(getHeaderValue(request, "X-Request-ID"))
                .username(request.getHeader("X-Username"))
                .userUuid(request.getHeader("X-User-UUID"))
                .userEmail(request.getHeader("X-User-Email"))
                .authorities(request.getHeader("X-User-Authorities"))
                .viaInterceptor("true".equals(request.getHeader("X-Via-Interceptor")))
                .directAccess("true".equals(request.getHeader("X-Direct-Access")))
                .enriched("true".equals(request.getHeader("X-Enriched")))
                .timestamp(Instant.now())
                .build();
    }

    private String getHeaderValue(HttpServletRequest request, String headerName) {
        return Optional.ofNullable(request.getHeader(headerName))
                .orElse(UUID.randomUUID().toString());
    }

    private void logRequestDetails(RequestContext context, String operation) {
        log.info("Operation: {} - Correlation-ID: {}, Request-ID: {}, Via-Interceptor: {}, " +
                        "Direct-Access: {}, Enriched: {}, User: {}",
                operation, context.getCorrelationId(), context.getRequestId(),
                context.isViaInterceptor(), context.isDirectAccess(), context.isEnriched(),
                context.getUsername());
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

    private Map<String, Object> createSuccessResponse(String message, Object data, RequestContext context) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("timestamp", context.getTimestamp().toString());
        response.put("correlationId", context.getCorrelationId());
        response.put("requestId", context.getRequestId());
        response.put("processedBy", "product-service");
        response.put("viaInterceptor", context.isViaInterceptor());
        response.put("directAccess", context.isDirectAccess());
        response.put("enriched", context.isEnriched());

        if (data != null) {
            response.put("data", data);
        }

        return response;
    }

    private Map<String, Object> createErrorResponse(String message, RequestContext context, Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("timestamp", context.getTimestamp().toString());
        response.put("correlationId", context.getCorrelationId());
        response.put("requestId", context.getRequestId());
        response.put("details", e.getMessage());
        return response;
    }

    private Map<String, Object> createUserContext(RequestContext context) {
        Map<String, Object> userContext = new HashMap<>();
        userContext.put("username", context.getUsername());
        userContext.put("uuid", context.getUserUuid());
        userContext.put("email", context.getUserEmail());
        userContext.put("authorities", context.getAuthorities());
        userContext.put("authenticated", context.getUsername() != null);
        return userContext;
    }

    private Map<String, Object> createRoutingInfo(RequestContext context) {
        Map<String, Object> routingInfo = new HashMap<>();
        routingInfo.put("viaInterceptor", context.isViaInterceptor());
        routingInfo.put("directAccess", context.isDirectAccess());
        routingInfo.put("enriched", context.isEnriched());
        routingInfo.put("source", context.isViaInterceptor() ? "gateway-helper-product" : "gateway-direct-product");
        return routingInfo;
    }
}


