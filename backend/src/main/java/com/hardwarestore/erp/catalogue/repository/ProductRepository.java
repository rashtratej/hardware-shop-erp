
package com.hardwarestore.erp.catalogue.repository;

import com.hardwarestore.erp.catalogue.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends
        JpaRepository<Product, UUID>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<Product> {

    List<Product> findAllByActiveTrueOrderByNameAsc();

    Optional<Product> findByIdAndActiveTrue(UUID id);

    Optional<Product> findBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, UUID id);

    @Override
    @EntityGraph(attributePaths = {"category", "uom", "gstRate"})
    Page<Product> findAll(
            Specification<Product> specification,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT p FROM Product p
       WHERE p.id = :id AND p.active = true
       """)
    Optional<Product> findActiveByIdForUpdate(@Param("id") UUID id);
}
