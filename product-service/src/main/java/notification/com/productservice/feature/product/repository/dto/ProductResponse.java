package notification.com.productservice.feature.product.repository.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        String category,
        String imageUrl,
        Boolean isActive,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate
) {}

