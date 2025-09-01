package notification.com.productservice.utils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.com.productservice.domain.Product;
import notification.com.productservice.feature.product.repository.ProductRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final ProductRepository productRepository;

    @PostConstruct
    public void initData() {
        if (productRepository.count() == 0) {
            log.info("Initializing sample product data...");

            List<Product> sampleProducts = Arrays.asList(
                    createProduct("Laptop", "High-performance laptop for developers",
                            new BigDecimal("1299.99"), 50, "Electronics"),
                    createProduct("Smartphone", "Latest smartphone with advanced features",
                            new BigDecimal("699.99"), 100, "Electronics"),
                    createProduct("Coffee Maker", "Premium coffee maker for home use",
                            new BigDecimal("199.99"), 25, "Appliances"),
                    createProduct("Desk Chair", "Ergonomic office chair",
                            new BigDecimal("299.99"), 30, "Furniture"),
                    createProduct("Wireless Headphones", "Noise-cancelling wireless headphones",
                            new BigDecimal("149.99"), 75, "Electronics")
            );

            productRepository.saveAll(sampleProducts);
            log.info("Sample product data initialized successfully");
        }
    }

    private Product createProduct(String name, String description, BigDecimal price,
                                  Integer quantity, String category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setCategory(category);
        product.setImageUrl("https://via.placeholder.com/300x200");
        product.setIsActive(true);
        return product;
    }
}

