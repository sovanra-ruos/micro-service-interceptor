package notification.com.helperservice.feature.log.controller;

import notification.com.helperservice.feature.log.entity.RequestLog;
import notification.com.helperservice.feature.log.service.RequestLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class RequestLogController {

    private final RequestLogService requestLogService;

    @GetMapping
    public ResponseEntity<List<RequestLog>> getAllLogs() {
        return ResponseEntity.ok(requestLogService.getLogsByDateRange(
            LocalDateTime.now().minusDays(7), LocalDateTime.now()));
    }

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<RequestLog>> getLogsByService(@PathVariable String serviceName) {
        return ResponseEntity.ok(requestLogService.getLogsByService(serviceName));
    }

    @GetMapping("/errors")
    public ResponseEntity<List<RequestLog>> getErrorLogs() {
        return ResponseEntity.ok(requestLogService.getErrorLogs());
    }

    @GetMapping("/errors/since")
    public ResponseEntity<List<RequestLog>> getErrorLogsSince(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        return ResponseEntity.ok(requestLogService.getErrorLogsSince(since));
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<RequestLog> getLogByRequestId(@PathVariable String requestId) {
        Optional<RequestLog> log = requestLogService.getLogByRequestId(requestId);
        return log.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<RequestLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(requestLogService.getLogsByDateRange(start, end));
    }

    @GetMapping("/service/{serviceName}/date-range")
    public ResponseEntity<List<RequestLog>> getLogsByServiceAndDateRange(
            @PathVariable String serviceName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(requestLogService.getLogsByServiceAndDateRange(serviceName, start, end));
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        Map<String, Object> analytics = Map.of(
            "totalRequests", requestLogService.getTotalRequestsCount(),
            "totalErrors", requestLogService.getErrorCount(),
            "requestsLast24Hours", requestLogService.getRequestCountByDateRange(
                LocalDateTime.now().minusDays(1), LocalDateTime.now()),
            "errorsLast24Hours", requestLogService.getErrorLogsSince(
                LocalDateTime.now().minusDays(1)).size()
        );
        return ResponseEntity.ok(analytics);
    }
}
