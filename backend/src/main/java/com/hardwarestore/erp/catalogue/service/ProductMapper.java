
package com.hardwarestore.erp.catalogue.service;

import com.hardwarestore.erp.catalogue.dto.ProductResponse;
import com.hardwarestore.erp.catalogue.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getHsnCode(),

                product.getCategory() != null
                        ? product.getCategory().getId()
                        : null,

                product.getCategory() != null
                        ? product.getCategory().getName()
                        : null,

                product.getUom().getId(),
                product.getUom().getCode(),
                product.getUom().getDisplayName(),

                product.getGstRate().getId(),
                product.getGstRate().getCode(),
                product.getGstRate().getGstRate(),

                product.getReorderLevel(),
                product.getDefaultSalePrice(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
