package notification.com.productservice.feature.product.repository.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
        BigDecimal price,

        @Min(value = 0, message = "Quantity cannot be negative")
        Integer quantity,

        @Size(max = 50, message = "Category cannot exceed 50 characters")
        String category,

        @Size(max = 255, message = "Image URL cannot exceed 255 characters")
        String imageUrl,

        Boolean isActive
) {}

