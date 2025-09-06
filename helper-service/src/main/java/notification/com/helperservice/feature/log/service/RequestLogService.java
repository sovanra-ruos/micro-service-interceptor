package notification.com.helperservice.feature.log.service;

import notification.com.helperservice.feature.log.entity.RequestLog;
import notification.com.helperservice.feature.log.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestLogService {

    private final RequestLogRepository requestLogRepository;

    public RequestLog createRequestLog(String serviceName, String method, String endpoint,
                                     String requestUrl, Map<String, String> headers,
                                     String requestBody, String clientIp, String userAgent) {
        RequestLog requestLog = RequestLog.builder()
                .requestId(UUID.randomUUID().toString())
                .serviceName(serviceName)
                .method(method)
                .endpoint(endpoint)
                .requestUrl(requestUrl)
                .timestamp(LocalDateTime.now())
                .requestHeaders(headers)
                .requestBody(requestBody)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .status("PENDING")
                .build();

        return requestLogRepository.save(requestLog);
    }

    public void updateResponseLog(String requestId, Integer responseStatus,
                                Map<String, String> responseHeaders, String responseBody,
                                Long duration, String status) {
        Optional<RequestLog> optionalLog = requestLogRepository.findByRequestId(requestId);
        if (optionalLog.isPresent()) {
            RequestLog log = optionalLog.get();
            log.setResponseStatus(responseStatus);
            log.setResponseHeaders(responseHeaders);
            log.setResponseBody(responseBody);
            log.setDuration(duration);
            log.setStatus(status);
            requestLogRepository.save(log);
        } else {
            log.warn("Request log not found for requestId: {}", requestId);
        }
    }

    public void updateErrorLog(String requestId, String errorMessage, Long duration) {
        Optional<RequestLog> optionalLog = requestLogRepository.findByRequestId(requestId);
        if (optionalLog.isPresent()) {
            RequestLog log = optionalLog.get();
            log.setErrorMessage(errorMessage);
            log.setDuration(duration);
            log.setStatus("ERROR");
            requestLogRepository.save(log);
        } else {
            log.warn("Request log not found for requestId: {}", requestId);
        }
    }

    public List<RequestLog> getLogsByService(String serviceName) {
        return requestLogRepository.findByServiceName(serviceName);
    }

    public List<RequestLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return requestLogRepository.findByTimestampBetween(start, end);
    }

    public List<RequestLog> getLogsByServiceAndDateRange(String serviceName, LocalDateTime start, LocalDateTime end) {
        return requestLogRepository.findByServiceNameAndTimestampBetween(serviceName, start, end);
    }

    public List<RequestLog> getErrorLogs() {
        return requestLogRepository.findByStatus("ERROR");
    }

    public List<RequestLog> getErrorLogsSince(LocalDateTime since) {
        return requestLogRepository.findErrorsSince(since);
    }

    public Optional<RequestLog> getLogByRequestId(String requestId) {
        return requestLogRepository.findByRequestId(requestId);
    }

    // Analytics methods
    public long getTotalRequestsCount() {
        return requestLogRepository.count();
    }

    public long getRequestCountByService(String serviceName) {
        return requestLogRepository.countByServiceName(serviceName);
    }

    public long getErrorCount() {
        return requestLogRepository.countByStatus("ERROR");
    }

    public long getRequestCountByDateRange(LocalDateTime start, LocalDateTime end) {
        return requestLogRepository.countByTimestampBetween(start, end);
    }
}
