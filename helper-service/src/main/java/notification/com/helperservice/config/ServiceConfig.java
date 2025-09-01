package notification.com.helperservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Configuration
@ConfigurationProperties(prefix = "services")
public class ServiceConfig {

    private Map<String, ServiceInfo> targets = new HashMap<>();

    @Data
    public static class ServiceInfo {
        private String url;
        private String basePath;
        private int timeout = 30;
        private boolean requiresAuth = true;
        private Set<String> allowedMethods = Set.of("GET", "POST", "PUT", "DELETE");
    }

    public ServiceInfo getServiceInfo(String serviceName) {
        return targets.get(serviceName);
    }
}



