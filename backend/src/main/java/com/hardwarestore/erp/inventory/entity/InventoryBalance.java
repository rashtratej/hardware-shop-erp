
package com.hardwarestore.erp.inventory.entity;

import com.hardwarestore.erp.catalogue.entity.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_balances")
public class InventoryBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "quantity_on_hand", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantityReserved;

    @Column(name = "available_quantity", insertable = false, updatable = false)
    private BigDecimal availableQuantity;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false)
    private Instant updatedAt;

    protected InventoryBalance() {}

    public static InventoryBalance create(Product product) {
        InventoryBalance balance = new InventoryBalance();
        balance.product = product;
        balance.quantityOnHand = BigDecimal.ZERO;
        balance.quantityReserved = BigDecimal.ZERO;
        return balance;
    }

    public void applyChange(BigDecimal change) {
        BigDecimal newQuantity = quantityOnHand.add(change);

        if (newQuantity.compareTo(quantityReserved) < 0) {
            throw new IllegalArgumentException(
                    "Stock cannot fall below the reserved quantity"
            );
        }

        this.quantityOnHand = newQuantity;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public BigDecimal getQuantityOnHand() { return quantityOnHand; }
    public BigDecimal getQuantityReserved() { return quantityReserved; }
    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
