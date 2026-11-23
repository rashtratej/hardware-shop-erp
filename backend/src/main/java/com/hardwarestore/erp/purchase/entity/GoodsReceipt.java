
package com.hardwarestore.erp.purchase.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "goods_receipts")
public class GoodsReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "receipt_number", nullable = false,
            unique = true, length = 30,
            insertable = false, updatable = false)
    private String receiptNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "supplier_delivery_note", length = 100)
    private String supplierDeliveryNote;

    @Column(length = 1000)
    private String notes;

    @Column(name = "received_by", nullable = false, updatable = false)
    private UUID receivedBy;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private Instant createdAt;

    protected GoodsReceipt() {
    }

    public static GoodsReceipt create(
            PurchaseOrder purchaseOrder,
            LocalDate receiptDate,
            String supplierDeliveryNote,
            String notes,
            UUID receivedBy
    ) {
        GoodsReceipt receipt = new GoodsReceipt();
        receipt.purchaseOrder = purchaseOrder;
        receipt.receiptDate = receiptDate;
        receipt.supplierDeliveryNote = supplierDeliveryNote;
        receipt.notes = notes;
        receipt.receivedBy = receivedBy;
        return receipt;
    }

    public UUID getId() { return id; }
    public String getReceiptNumber() { return receiptNumber; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public LocalDate getReceiptDate() { return receiptDate; }
    public String getSupplierDeliveryNote() { return supplierDeliveryNote; }
    public String getNotes() { return notes; }
    public UUID getReceivedBy() { return receivedBy; }
    public Instant getCreatedAt() { return createdAt; }
}
