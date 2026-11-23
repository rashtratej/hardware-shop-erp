package com.hardwarestore.erp.purchase.repository;

import com.hardwarestore.erp.purchase.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoodsReceiptRepository
        extends JpaRepository<GoodsReceipt, UUID> {

    List<GoodsReceipt> findByPurchaseOrderIdOrderByReceiptDateDesc(
            UUID purchaseOrderId
    );

    boolean existsByReceiptNumber(String receiptNumber);
}
