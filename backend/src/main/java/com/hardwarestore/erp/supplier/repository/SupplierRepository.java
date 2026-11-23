
package com.hardwarestore.erp.supplier.repository;

import com.hardwarestore.erp.supplier.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    boolean existsByGstin(String gstin);

    boolean existsByGstinAndIdNot(String gstin, UUID id);

    Optional<Supplier> findByIdAndActiveTrue(UUID id);

    @Query("""
            SELECT s FROM Supplier s
            WHERE (:active IS NULL OR s.active = :active)
              AND (
                  :search = ''
                  OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(s.gstin, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(s.phone, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(s.email, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<Supplier> search(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
