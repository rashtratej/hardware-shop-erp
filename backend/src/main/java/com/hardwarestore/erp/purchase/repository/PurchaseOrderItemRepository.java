package com.hardwarestore.erp.purchase.repository;

import com.hardwarestore.erp.purchase.entity.PurchaseOrderItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderItemRepository
        extends JpaRepository<PurchaseOrderItem, UUID> {

    List<PurchaseOrderItem> findByPurchaseOrderId(UUID purchaseOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT item FROM PurchaseOrderItem item
            WHERE item.id = :id
            """)
    Optional<PurchaseOrderItem> findByIdForUpdate(@Param("id") UUID id);
}
