
package com.hardwarestore.erp.catalogue.service;

import com.hardwarestore.erp.catalogue.category.entity.ProductCategory;
import com.hardwarestore.erp.catalogue.category.repository.ProductCategoryRepository;
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
import com.hardwarestore.erp.catalogue.dto.UpdateProductRequest;

import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import com.hardwarestore.erp.catalogue.dto.ChangeProductStatusRequest;
import com.hardwarestore.erp.catalogue.dto.ProductPageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UnitOfMeasurementRepository uomRepository;
    private final GstRateRepository gstRateRepository;
    private final ProductMapper productMapper;
    private final ProductCategoryRepository categoryRepository;
    private final EntityManager entityManager;

    public ProductService(
            ProductRepository productRepository,
            UnitOfMeasurementRepository uomRepository,
            GstRateRepository gstRateRepository,
            ProductCategoryRepository categoryRepository,
            ProductMapper productMapper,
            EntityManager entityManager
    ) {
        this.productRepository = productRepository;
        this.uomRepository = uomRepository;
        this.gstRateRepository = gstRateRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
        this.entityManager = entityManager;
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

        ProductCategory category = null;

        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .filter(ProductCategory::isActive)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active product category not found"
                    ));
        }

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
                category,
                uom,
                gstRate,
                request.reorderLevel(),
                request.defaultSalePrice(),
                createdBy
        );

        Product savedProduct = productRepository.save(product);
        entityManager.flush();
        entityManager.refresh(savedProduct);
        return productMapper.toResponse(savedProduct);
    }


    @Transactional
    public ProductResponse updateProduct(
            UUID id,
            UpdateProductRequest request
    ) {

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active product not found"
                ));

        String sku = request.sku().trim();

        if (productRepository.existsBySkuIgnoreCaseAndIdNot(sku, id)) {
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

        ProductCategory category = null;

        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .filter(ProductCategory::isActive)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active product category not found"
                    ));
        }

        product.update(
                sku,
                request.name(),
                request.description(),
                request.hsnCode(),
                category,
                uom,
                gstRate,
                request.reorderLevel(),
                request.defaultSalePrice()
        );

        entityManager.flush();
        entityManager.refresh(product);

        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse changeProductStatus(
            UUID id,
            ChangeProductStatusRequest request
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found"
                ));

        product.changeActiveStatus(request.active());

        entityManager.flush();
        entityManager.refresh(product);

        return productMapper.toResponse(product);
    }


    @Transactional(readOnly = true)
    public ProductPageResponse searchProducts(
            int page,
            int size,
            String search,
            UUID categoryId,
            UUID uomId,
            boolean active,
            String sortBy,
            String direction
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be >= 0 and size must be between 1 and 100"
            );
        }

        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );

        if (!active && !isAdmin) {
            throw new AccessDeniedException(
                    "Only administrators can view inactive products"
            );
        }

        java.util.Set<String> allowedSortFields = java.util.Set.of(
                "name",
                "sku",
                "createdAt",
                "updatedAt",
                "defaultSalePrice",
                "reorderLevel"
        );

        if (!allowedSortFields.contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort field"
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Direction must be asc or desc"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        String keyword = search == null || search.isBlank()
                ? null
                : search.trim().toLowerCase();

        Specification<Product> specification = (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("active"), active));

            if (keyword != null) {
                String pattern = "%" + keyword + "%";

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("sku")), pattern),
                        cb.like(cb.lower(root.get("name")), pattern)
                ));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(
                        root.get("category").get("id"),
                        categoryId
                ));
            }

            if (uomId != null) {
                predicates.add(cb.equal(
                        root.get("uom").get("id"),
                        uomId
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage =
                productRepository.findAll(specification, pageable);

        Page<ProductResponse> responsePage =
                productPage.map(productMapper::toResponse);

        return new ProductPageResponse(
                responsePage.getContent(),
                responsePage.getNumber(),
                responsePage.getSize(),
                responsePage.getTotalElements(),
                responsePage.getTotalPages(),
                responsePage.isFirst(),
                responsePage.isLast(),
                responsePage.hasNext(),
                responsePage.hasPrevious()
        );
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