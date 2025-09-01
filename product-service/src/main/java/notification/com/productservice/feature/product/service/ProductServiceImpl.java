package notification.com.productservice.feature.product.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import notification.com.productservice.domain.Product;
import notification.com.productservice.feature.product.mapper.ProductMapper;
import notification.com.productservice.feature.product.repository.ProductRepository;
import notification.com.productservice.feature.product.repository.dto.ProductCreateRequest;
import notification.com.productservice.feature.product.repository.dto.ProductResponse;
import notification.com.productservice.feature.product.repository.dto.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, String createdBy) {

        Product product = productMapper.fromProductCreateRequest(request);
        product = productRepository.save(product);

        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + id
                ));

        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.findAllByIsActiveTrue(pageable);
        return products.map(productMapper::toProductResponse);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request, String updatedBy) {

        Product product = productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + id
                ));

        productMapper.updateProductFromRequest(request, product);
        product = productRepository.save(product);

        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, String deletedBy) {

        Product product = productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + id
                ));

        product.setIsActive(false);
        productRepository.save(product);

    }

    @Override
    @Transactional
    public List<ProductResponse> getProductsByCategory(String category) {

        List<Product> products = productRepository.findByCategoryAndIsActiveTrue(category);
        return products.stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Page<ProductResponse> searchProducts(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Product> products = productRepository.searchProducts(keyword, pageable);

        return products.map(productMapper::toProductResponse);
    }

    @Override
    @Transactional
    public Page<ProductResponse> getProductsWithFilters(BigDecimal minPrice, BigDecimal maxPrice,
                                                        String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Product> products = productRepository.findWithFilters(minPrice, maxPrice, category, pageable);

        return products.map(productMapper::toProductResponse);
    }
}
