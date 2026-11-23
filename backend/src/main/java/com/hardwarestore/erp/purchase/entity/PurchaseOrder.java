package com.hardwarestore.erp.purchase.entity;

import com.hardwarestore.erp.supplier.entity.Supplier;
import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "po_number", nullable = false, unique = true, length = 30,
            insertable = false, updatable = false)
    private String poNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PurchaseOrderStatus status;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxTotal;

    @Column(name = "grand_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal grandTotal;

    @Column(length = 1000)
    private String notes;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "rejected_by")
    private UUID rejectedBy;

    @Column(name = "cancelled_by")
    private UUID cancelledBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false,
            insertable = false)
    private Instant updatedAt;


    protected PurchaseOrder() {
    }

    public static PurchaseOrder create(
            Supplier supplier,
            LocalDate orderDate,
            LocalDate expectedDeliveryDate,
            String notes,
            UUID createdBy
    ) {
        PurchaseOrder order = new PurchaseOrder();
        order.supplier = supplier;
        order.orderDate = orderDate;
        order.expectedDeliveryDate = expectedDeliveryDate;
        order.notes = notes;
        order.createdBy = createdBy;
        order.status = PurchaseOrderStatus.DRAFT;
        order.subtotal = BigDecimal.ZERO.setScale(2);
        order.taxTotal = BigDecimal.ZERO.setScale(2);
        order.grandTotal = BigDecimal.ZERO.setScale(2);
        return order;
    }

    public void updateTotals(
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal grandTotal
    ) {
        requireStatus(PurchaseOrderStatus.DRAFT);
        this.subtotal = subtotal;
        this.taxTotal = taxTotal;
        this.grandTotal = grandTotal;
    }

    public void updateDraftDetails(
            Supplier supplier,
            LocalDate orderDate,
            LocalDate expectedDeliveryDate,
            String notes
    ) {
        requireStatus(PurchaseOrderStatus.DRAFT);
        this.supplier = supplier;
        this.orderDate = orderDate;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.notes = notes;
    }

    public void submitForApproval() {
        requireStatus(PurchaseOrderStatus.DRAFT);
        this.status = PurchaseOrderStatus.PENDING_APPROVAL;
    }

    public void approve(UUID userId) {
        requireStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        this.status = PurchaseOrderStatus.APPROVED;
        this.approvedBy = userId;
        this.approvedAt = Instant.now();
    }

    public void reject(UUID userId, String reason) {
        requireStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        this.status = PurchaseOrderStatus.REJECTED;
        this.rejectedBy = userId;
        this.rejectedAt = Instant.now();
        this.rejectionReason = reason;
    }

    public void cancel(UUID userId, String reason) {
        if (status != PurchaseOrderStatus.DRAFT
                && status != PurchaseOrderStatus.PENDING_APPROVAL
                && status != PurchaseOrderStatus.APPROVED) {
            throw new IllegalStateException(
                    "Purchase order cannot be cancelled in its current status"
            );
        }

        this.status = PurchaseOrderStatus.CANCELLED;
        this.cancelledBy = userId;
        this.cancelledAt = Instant.now();
        this.cancellationReason = reason;
    }

    public void markPartiallyReceived() {
        if (status != PurchaseOrderStatus.APPROVED
                && status != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new IllegalStateException(
                    "Purchase order is not eligible for goods receipt"
            );
        }
        this.status = PurchaseOrderStatus.PARTIALLY_RECEIVED;
    }

    public void markReceived() {
        if (status != PurchaseOrderStatus.APPROVED
                && status != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new IllegalStateException(
                    "Purchase order is not eligible for completion"
            );
        }
        this.status = PurchaseOrderStatus.RECEIVED;
    }

    private void requireStatus(PurchaseOrderStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Purchase order must be " + expected
            );
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getPoNumber() { return poNumber; }
    public Supplier getSupplier() { return supplier; }
    public LocalDate getOrderDate() { return orderDate; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public PurchaseOrderStatus getStatus() { return status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTaxTotal() { return taxTotal; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public String getNotes() { return notes; }
    public String getRejectionReason() { return rejectionReason; }
    public String getCancellationReason() { return cancellationReason; }
    public UUID getCreatedBy() { return createdBy; }
    public UUID getApprovedBy() { return approvedBy; }
    public UUID getRejectedBy() { return rejectedBy; }
    public UUID getCancelledBy() { return cancelledBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public Instant getRejectedAt() { return rejectedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
