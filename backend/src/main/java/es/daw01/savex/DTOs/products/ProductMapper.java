package es.daw01.savex.DTOs.products;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import es.daw01.savex.model.Product;
import es.daw01.savex.model.SupermarketType;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    
    @Mapping(target = "product_id", source = "productId")
    @Mapping(target = "product_url", source = "productUrl")
    @Mapping(target = "product_type", source = "productType")
    @Mapping(target = "product_categories", source = "productCategories")
    @Mapping(target = "price.total", source = "priceTotal")
    @Mapping(target = "price.per_reference_unit", source = "pricePerReferenceUnit")
    @Mapping(target = "price.reference_units", source = "referenceUnits")
    @Mapping(target = "price.reference_unit_name", source = "referenceUnitName")
    @Mapping(target = "supermarket_name", expression = "java(product.getSupermarket().getName())")
    @Mapping(target = "display_name", source = "name")
    @Mapping(target = "normalized_name", source = "normalizedName")
    ProductDTO toDTO(Product product);
    List<ProductDTO> toDTOs(List<Product> products);

    @Mapping(target = "id", source = "product_id")
    @Mapping(target = "productId", source = "product_id")
    @Mapping(target = "productUrl", source = "product_url")
    @Mapping(target = "productType", source = "product_type")
    @Mapping(target = "productCategories", source = "product_categories")
    @Mapping(target = "priceTotal", source = "price.total")
    @Mapping(target = "pricePerReferenceUnit", source = "price.per_reference_unit")
    @Mapping(target = "referenceUnits", source = "price.reference_units")
    @Mapping(target = "referenceUnitName", source = "price.reference_unit_name")
    @Mapping(target = "supermarket", ignore = true)
    @Mapping(target = "name", source = "display_name")
    @Mapping(target = "normalizedName", source = "normalized_name")
    Product toEntity(ProductDTO productDTO);
    List<Product> toEntities(List<ProductDTO> productDTOs);

    @AfterMapping
    default void setSupermarket(@MappingTarget Product product, ProductDTO productDTO) {
        product.setSupermarket(SupermarketType.fromString(productDTO.supermarket_name().toUpperCase()));
    }
}
