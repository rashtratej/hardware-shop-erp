
package com.hardwarestore.erp.inventory.entity;

import com.hardwarestore.erp.catalogue.entity.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "movement_type", nullable = false, length = 20)
    private String movementType;

    @Column(name = "quantity_change", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantityChange;

    @Column(name = "reference_type", length = 40)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected StockMovement() {}

    public static StockMovement create(
            Product product,
            String movementType,
            BigDecimal quantityChange,
            String referenceType,
            UUID referenceId,
            String notes,
            UUID createdBy
    ) {
        StockMovement movement = new StockMovement();
        movement.product = product;
        movement.movementType = movementType;
        movement.quantityChange = quantityChange;
        movement.referenceType = referenceType;
        movement.referenceId = referenceId;
        movement.notes = notes;
        movement.createdBy = createdBy;
        return movement;
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public String getMovementType() { return movementType; }
    public BigDecimal getQuantityChange() { return quantityChange; }
    public String getReferenceType() { return referenceType; }
    public UUID getReferenceId() { return referenceId; }
    public String getNotes() { return notes; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}
