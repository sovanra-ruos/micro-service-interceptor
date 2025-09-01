package notification.com.productservice.feature.product.service;

import notification.com.productservice.feature.product.repository.dto.ProductCreateRequest;
import notification.com.productservice.feature.product.repository.dto.ProductResponse;
import notification.com.productservice.feature.product.repository.dto.ProductUpdateRequest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductCreateRequest request, String createdBy);
    ProductResponse getProductById(Long id);
    Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir);
    ProductResponse updateProduct(Long id, ProductUpdateRequest request, String updatedBy);
    void deleteProduct(Long id, String deletedBy);
    List<ProductResponse> getProductsByCategory(String category);
    Page<ProductResponse> searchProducts(String keyword, int page, int size);
    Page<ProductResponse> getProductsWithFilters(BigDecimal minPrice, BigDecimal maxPrice,
                                                 String category, int page, int size);
}
