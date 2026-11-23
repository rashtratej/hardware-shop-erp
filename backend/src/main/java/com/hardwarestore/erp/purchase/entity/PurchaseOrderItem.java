package com.hardwarestore.erp.purchase.entity;

import com.hardwarestore.erp.catalogue.entity.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku_snapshot", nullable = false)
    private String skuSnapshot;

    @Column(name = "product_name_snapshot", nullable = false, length = 180)
    private String productNameSnapshot;

    @Column(name = "hsn_code_snapshot", nullable = false, length = 12)
    private String hsnCodeSnapshot;

    @Column(name = "uom_code_snapshot", nullable = false, length = 16)
    private String uomCodeSnapshot;

    @Column(name = "ordered_quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal orderedQuantity;

    @Column(name = "received_quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal receivedQuantity;

    @Column(name = "unit_cost", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "gst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal gstRate;

    @Column(name = "taxable_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxableAmount;

    @Column(name = "tax_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "line_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineTotal;

    protected PurchaseOrderItem() {
    }

    public static PurchaseOrderItem create(
            PurchaseOrder purchaseOrder,
            Product product,
            BigDecimal orderedQuantity,
            BigDecimal unitCost,
            BigDecimal gstRate,
            BigDecimal taxableAmount,
            BigDecimal taxAmount,
            BigDecimal lineTotal
    ) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.purchaseOrder = purchaseOrder;
        item.product = product;
        item.skuSnapshot = product.getSku();
        item.productNameSnapshot = product.getName();
        item.hsnCodeSnapshot = product.getHsnCode();
        item.uomCodeSnapshot = product.getUom().getCode();
        item.orderedQuantity = orderedQuantity;
        item.receivedQuantity = BigDecimal.ZERO.setScale(3);
        item.unitCost = unitCost;
        item.gstRate = gstRate;
        item.taxableAmount = taxableAmount;
        item.taxAmount = taxAmount;
        item.lineTotal = lineTotal;
        return item;
    }

    public void recordReceivedQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Received quantity must be greater than zero"
            );
        }

        BigDecimal newReceived = receivedQuantity.add(quantity);

        if (newReceived.compareTo(orderedQuantity) > 0) {
            throw new IllegalArgumentException(
                    "Received quantity cannot exceed outstanding quantity"
            );
        }

        this.receivedQuantity = newReceived;
    }

    public BigDecimal getOutstandingQuantity() {
        return orderedQuantity.subtract(receivedQuantity);
    }

    public UUID getId() { return id; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public Product getProduct() { return product; }
    public String getSkuSnapshot() { return skuSnapshot; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public String getHsnCodeSnapshot() { return hsnCodeSnapshot; }
    public String getUomCodeSnapshot() { return uomCodeSnapshot; }
    public BigDecimal getOrderedQuantity() { return orderedQuantity; }
    public BigDecimal getReceivedQuantity() { return receivedQuantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getGstRate() { return gstRate; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
