package notification.com.productservice.feature.product.mapper;

import notification.com.productservice.domain.Product;
import notification.com.productservice.feature.product.repository.dto.ProductCreateRequest;
import notification.com.productservice.feature.product.repository.dto.ProductResponse;
import notification.com.productservice.feature.product.repository.dto.ProductUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    ProductResponse toProductResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Product fromProductCreateRequest(ProductCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    void updateProductFromRequest(ProductUpdateRequest request, @MappingTarget Product product);
}