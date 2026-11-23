package com.hardwarestore.erp.purchase.repository;

import com.hardwarestore.erp.purchase.entity.PurchaseOrder;
import com.hardwarestore.erp.purchase.entity.PurchaseOrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT po FROM PurchaseOrder po WHERE po.id = :id")
    Optional<PurchaseOrder> findByIdForUpdate(@Param("id") UUID id);

    @EntityGraph(attributePaths = "supplier")
    @Query("""
            SELECT po FROM PurchaseOrder po
            WHERE (:status IS NULL OR po.status = :status)
              AND (:supplierId IS NULL OR po.supplier.id = :supplierId)
            """)
    Page<PurchaseOrder> search(
            @Param("status") PurchaseOrderStatus status,
            @Param("supplierId") UUID supplierId,
            Pageable pageable
    );

    boolean existsByPoNumber(String poNumber);
}