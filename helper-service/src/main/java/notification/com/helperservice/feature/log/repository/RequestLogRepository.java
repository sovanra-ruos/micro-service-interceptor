package notification.com.helperservice.feature.log.repository;

import notification.com.helperservice.feature.log.entity.RequestLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequestLogRepository extends MongoRepository<RequestLog, String> {

    List<RequestLog> findByServiceName(String serviceName);

    List<RequestLog> findByStatus(String status);

    List<RequestLog> findByClientIp(String clientIp);

    List<RequestLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<RequestLog> findByServiceNameAndTimestampBetween(String serviceName, LocalDateTime start, LocalDateTime end);

    Optional<RequestLog> findByRequestId(String requestId);

    @Query("{'serviceName': ?0, 'status': ?1}")
    List<RequestLog> findByServiceNameAndStatus(String serviceName, String status);

    @Query("{'timestamp': {$gte: ?0}, 'status': 'ERROR'}")
    List<RequestLog> findErrorsSince(LocalDateTime since);

    // Count methods for analytics
    long countByServiceName(String serviceName);

    long countByStatus(String status);

    long countByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
