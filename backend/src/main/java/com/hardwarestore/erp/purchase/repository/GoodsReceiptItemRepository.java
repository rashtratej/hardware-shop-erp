package com.hardwarestore.erp.purchase.repository;

import com.hardwarestore.erp.purchase.entity.GoodsReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoodsReceiptItemRepository
        extends JpaRepository<GoodsReceiptItem, UUID> {

    List<GoodsReceiptItem> findByGoodsReceiptId(UUID goodsReceiptId);
}
