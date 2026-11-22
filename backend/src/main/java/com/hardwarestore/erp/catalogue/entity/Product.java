
package com.hardwarestore.erp.catalogue.entity;

import jakarta.persistence.Column;
import com.hardwarestore.erp.catalogue.category.entity.ProductCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "sku", nullable = false, unique = true,
            columnDefinition = "citext", length = 255)
    private String sku;

    @Column(name = "name", nullable = false, length = 180)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "hsn_code", nullable = false, length = 12)
    private String hsnCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uom_id", nullable = false)
    private UnitOfMeasurement uom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gst_rate_id", nullable = false)
    private GstRate gstRate;

    @Column(name = "reorder_level", nullable = false,
            precision = 14, scale = 3)
    private BigDecimal reorderLevel;

    @Column(name = "default_sale_price", nullable = false,
            precision = 14, scale = 2)
    private BigDecimal defaultSalePrice;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private Instant updatedAt;

    protected Product() {
    }

    public static Product create(
            String sku,
            String name,
            String description,
            String hsnCode,
            ProductCategory category,
            UnitOfMeasurement uom,
            GstRate gstRate,
            BigDecimal reorderLevel,
            BigDecimal defaultSalePrice,
            UUID createdBy
    ) {
        Product product = new Product();
        product.sku = sku.trim();
        product.name = name.trim();
        product.description = description;
        product.hsnCode = hsnCode.trim();
        product.category = category;
        product.uom = uom;
        product.gstRate = gstRate;
        product.reorderLevel = reorderLevel;
        product.defaultSalePrice = defaultSalePrice;
        product.createdBy = createdBy;
        product.active = true;
        return product;
    }


    public void update(
            String sku,
            String name,
            String description,
            String hsnCode,
            ProductCategory category,
            UnitOfMeasurement uom,
            GstRate gstRate,
            BigDecimal reorderLevel,
            BigDecimal defaultSalePrice
    ) {
        this.sku = sku.trim();
        this.name = name.trim();
        this.description = description;
        this.hsnCode = hsnCode.trim();
        this.category = category;
        this.uom = uom;
        this.gstRate = gstRate;
        this.reorderLevel = reorderLevel;
        this.defaultSalePrice = defaultSalePrice;
    }

    public void changeActiveStatus(boolean active) {
        this.active = active;
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHsnCode() {
        return hsnCode;
    }

    public UnitOfMeasurement getUom() {
        return uom;
    }

    public GstRate getGstRate() {
        return gstRate;
    }

    public BigDecimal getReorderLevel() {
        return reorderLevel;
    }

    public BigDecimal getDefaultSalePrice() {
        return defaultSalePrice;
    }

    public boolean isActive() {
        return active;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}