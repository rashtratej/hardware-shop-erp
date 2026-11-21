
package com.hardwarestore.erp.catalogue.service;

import com.hardwarestore.erp.catalogue.dto.CreateProductRequest;
import com.hardwarestore.erp.catalogue.dto.ProductResponse;
import com.hardwarestore.erp.catalogue.entity.GstRate;
import com.hardwarestore.erp.catalogue.entity.Product;
import com.hardwarestore.erp.catalogue.entity.UnitOfMeasurement;
import com.hardwarestore.erp.catalogue.repository.GstRateRepository;
import com.hardwarestore.erp.catalogue.repository.ProductRepository;
import com.hardwarestore.erp.catalogue.repository.UnitOfMeasurementRepository;
import com.hardwarestore.erp.common.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.hardwarestore.erp.common.exception.ResourceConflictException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UnitOfMeasurementRepository uomRepository;
    private final GstRateRepository gstRateRepository;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            UnitOfMeasurementRepository uomRepository,
            GstRateRepository gstRateRepository,
            ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.uomRepository = uomRepository;
        this.gstRateRepository = gstRateRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        String sku = request.sku().trim();

        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new ResourceConflictException(
                    "A product with this SKU already exists"
            );
        }

        UnitOfMeasurement uom = uomRepository.findById(request.uomId())
                .filter(UnitOfMeasurement::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active unit of measurement not found"
                ));

        GstRate gstRate = gstRateRepository.findById(request.gstRateId())
                .filter(GstRate::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active GST rate not found"
                ));

        UUID createdBy = UUID.fromString(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );

        Product product = Product.create(
                sku,
                request.name(),
                request.description(),
                request.hsnCode(),
                uom,
                gstRate,
                request.reorderLevel(),
                request.defaultSalePrice(),
                createdBy
        );

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public java.util.List<ProductResponse> getAllActiveProducts() {
        return productRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getActiveProductById(UUID id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active product not found"
                ));

        return productMapper.toResponse(product);
    }


}