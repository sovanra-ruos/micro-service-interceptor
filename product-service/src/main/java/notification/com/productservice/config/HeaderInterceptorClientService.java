package notification.com.productservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeaderInterceptorClientService {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.header-interceptor.url:http://helper-service}")
    private String headerInterceptorUrl;

    public Mono<Map<String, Object>> enrichHeaders(Map<String, String> headers) {
        log.info("Calling header interceptor service to enrich headers {}", headers);

        return webClientBuilder.build()
                .post()
                .uri(headerInterceptorUrl + "/api/v1/headers/enrich")
                .bodyValue(headers)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(response -> log.info("Headers enriched successfully"))
                .doOnError(error -> log.error("Failed to enrich headers: {}", error.getMessage()))
                .onErrorReturn(createErrorResponse());
    }

    private Map<String, Object> createErrorResponse() {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Header enrichment failed");
        errorResponse.put("fallback", true);
        return errorResponse;
    }
}
