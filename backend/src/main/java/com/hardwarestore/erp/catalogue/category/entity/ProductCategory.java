package com.hardwarestore.erp.catalogue.category.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_categories")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private ProductCategory parentCategory;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private Instant updatedAt;

    protected ProductCategory() {
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ProductCategory getParentCategory() { return parentCategory; }
    public boolean isActive() { return active; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }



    public static ProductCategory create(
            String name,
            String description,
            ProductCategory parentCategory,
            UUID createdBy
    ) {
        ProductCategory category = new ProductCategory();
        category.name = requireName(name);
        category.description = description;
        category.parentCategory = parentCategory;
        category.createdBy = createdBy;
        category.active = true;
        return category;
    }

    public void updateDetails(
            String name,
            String description,
            ProductCategory parentCategory
    ) {
        this.name = requireName(name);
        this.description = description;
        this.parentCategory = parentCategory;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }

        return name.trim();
    }


    public void changeStatus(boolean active) {
        this.active = active;
    }
}