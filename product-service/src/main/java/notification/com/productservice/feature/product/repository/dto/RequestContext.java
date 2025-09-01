package notification.com.productservice.feature.product.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestContext {
    private String correlationId;
    private String requestId;
    private String username;
    private String userUuid;
    private String userEmail;
    private String authorities;
    private boolean viaInterceptor;
    private boolean directAccess;
    private boolean enriched;
    private Instant timestamp;
}
