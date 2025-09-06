package notification.com.helperservice.config;

import notification.com.helperservice.feature.log.service.RequestLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private final RequestLogService requestLogService;
    private final ThreadLocal<Long> requestStartTime = new ThreadLocal<>();
    private final ThreadLocal<String> requestIdHolder = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            long startTime = System.currentTimeMillis();
            requestStartTime.set(startTime);

            // Skip logging for actuator endpoints
            if (request.getRequestURI().startsWith("/actuator")) {
                return true;
            }

            String serviceName = extractServiceName(request.getRequestURI());
            String method = request.getMethod();
            String endpoint = request.getRequestURI();
            String requestUrl = request.getRequestURL().toString();
            String clientIp = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            Map<String, String> headers = Collections.list(request.getHeaderNames())
                    .stream()
                    .collect(Collectors.toMap(
                            name -> name,
                            request::getHeader
                    ));

            String requestBody = getRequestBody(request);

            var requestLog = requestLogService.createRequestLog(
                    serviceName, method, endpoint, requestUrl,
                    headers, requestBody, clientIp, userAgent
            );

            requestIdHolder.set(requestLog.getRequestId());

        } catch (Exception e) {
            log.error("Error in preHandle: ", e);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        try {
            // Skip logging for actuator endpoints
            if (request.getRequestURI().startsWith("/actuator")) {
                return;
            }

            Long startTime = requestStartTime.get();
            String requestId = requestIdHolder.get();

            if (startTime != null && requestId != null) {
                long duration = System.currentTimeMillis() - startTime;

                Map<String, String> responseHeaders = response.getHeaderNames()
                        .stream()
                        .collect(Collectors.toMap(
                                name -> name,
                                response::getHeader
                        ));

                if (ex != null) {
                    requestLogService.updateErrorLog(requestId, ex.getMessage(), duration);
                } else {
                    String status = response.getStatus() >= 200 && response.getStatus() < 300 ? "SUCCESS" : "ERROR";
                    requestLogService.updateResponseLog(
                            requestId, response.getStatus(), responseHeaders,
                            null, duration, status
                    );
                }
            }
        } catch (Exception e) {
            log.error("Error in afterCompletion: ", e);
        } finally {
            requestStartTime.remove();
            requestIdHolder.remove();
        }
    }

    private String extractServiceName(String uri) {
        // Extract service name from URI pattern
        if (uri.startsWith("/api/v1/")) {
            String[] parts = uri.split("/");
            if (parts.length > 3) {
                return parts[3] + "-service";
            }
        }
        return "helper-service";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String getRequestBody(HttpServletRequest request) {
        // For simplicity, we'll skip body reading here to avoid consuming the stream
        // You can implement this if needed using a wrapper
        return null;
    }
}
