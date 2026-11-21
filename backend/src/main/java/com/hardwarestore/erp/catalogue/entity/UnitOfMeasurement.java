
package com.hardwarestore.erp.catalogue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "uoms")
public class UnitOfMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 16)
    private String code;

    @Column(name = "display_name", nullable = false, length = 40)
    private String displayName;

    @Column(name = "quantity_scale", nullable = false)
    private short quantityScale;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected UnitOfMeasurement() {
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public short getQuantityScale() {
        return quantityScale;
    }

    public boolean isActive() {
        return active;
    }
}
