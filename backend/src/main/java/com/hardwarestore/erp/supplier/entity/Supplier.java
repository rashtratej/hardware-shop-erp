package com.hardwarestore.erp.supplier.entity;

import com.hardwarestore.erp.supplier.dto.SupplierRequest;
import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 15)
    private String gstin;

    @Column(name = "contact_person", length = 120)
    private String contactPerson;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "address_line1", length = 200)
    private String addressLine1;

    @Column(name = "address_line2", length = 200)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "state_code", length = 2)
    private String stateCode;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private Instant createdAt;

    @Generated(event = EventType.INSERT)
    @Column(name = "updated_at", nullable = false,
            insertable = false)
    private Instant updatedAt;

    protected Supplier() {
    }

    public static Supplier create(SupplierRequest request, UUID createdBy) {
        Supplier supplier = new Supplier();
        supplier.apply(request);
        supplier.createdBy = createdBy;
        supplier.active = true;
        return supplier;
    }

    public void update(SupplierRequest request) {
        apply(request);
    }

    private void apply(SupplierRequest request) {
        this.name = request.name().trim();
        this.gstin = normalize(request.gstin()) == null
                ? null
                : request.gstin().trim().toUpperCase();
        this.contactPerson = normalize(request.contactPerson());
        this.phone = normalize(request.phone());
        this.email = normalize(request.email());
        this.addressLine1 = normalize(request.addressLine1());
        this.addressLine2 = normalize(request.addressLine2());
        this.city = normalize(request.city());
        this.state = normalize(request.state());
        this.stateCode = normalize(request.stateCode());
        this.postalCode = normalize(request.postalCode());
        this.notes = normalize(request.notes());
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getGstin() { return gstin; }
    public String getContactPerson() { return contactPerson; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddressLine1() { return addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getStateCode() { return stateCode; }
    public String getPostalCode() { return postalCode; }
    public String getNotes() { return notes; }
    public boolean isActive() { return active; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
