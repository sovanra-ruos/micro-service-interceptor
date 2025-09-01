package notification.com.helperservice.feature.header.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrichmentRequest {
    private Map<String, String> headers;
    private String targetService;
}