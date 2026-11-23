package com.hardwarestore.erp.purchase.entity;

import com.hardwarestore.erp.catalogue.entity.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "goods_receipt_items")
public class GoodsReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_item_id", nullable = false)
    private PurchaseOrderItem purchaseOrderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "received_quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal receivedQuantity;

    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private java.time.Instant createdAt;

    protected GoodsReceiptItem() {
    }

    public static GoodsReceiptItem create(
            GoodsReceipt goodsReceipt,
            PurchaseOrderItem purchaseOrderItem,
            BigDecimal receivedQuantity
    ) {
        GoodsReceiptItem item = new GoodsReceiptItem();
        item.goodsReceipt = goodsReceipt;
        item.purchaseOrderItem = purchaseOrderItem;
        item.product = purchaseOrderItem.getProduct();
        item.receivedQuantity = receivedQuantity;
        return item;
    }

    public UUID getId() { return id; }
    public GoodsReceipt getGoodsReceipt() { return goodsReceipt; }
    public PurchaseOrderItem getPurchaseOrderItem() { return purchaseOrderItem; }
    public Product getProduct() { return product; }
    public BigDecimal getReceivedQuantity() { return receivedQuantity; }
    public java.time.Instant getCreatedAt() { return createdAt; }
}
