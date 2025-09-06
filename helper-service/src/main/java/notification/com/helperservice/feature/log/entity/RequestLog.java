package notification.com.helperservice.feature.log.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "request_logs")
public class RequestLog {

    @Id
    private String id;

    @Indexed
    private String requestId;

    @Indexed
    private String serviceName;

    private String method;
    private String endpoint;
    private String requestUrl;

    @Indexed
    private LocalDateTime timestamp;

    private Map<String, String> requestHeaders;
    private String requestBody;

    private Integer responseStatus;
    private Map<String, String> responseHeaders;
    private String responseBody;

    private Long duration; // in milliseconds

    @Indexed
    private String status; // SUCCESS, ERROR, TIMEOUT

    private String errorMessage;

    @Indexed
    private String clientIp;

    private String userAgent;
}
