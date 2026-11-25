package com.hardwarestore.erp.customer.entity;

import com.hardwarestore.erp.customer.dto.CustomerRequest;
import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 180)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 20)
    private CustomerType customerType;

    @Column(length = 15)
    private String gstin;

    @Column(name = "contact_person", length = 120)
    private String contactPerson;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "billing_address_line1", length = 200)
    private String billingAddressLine1;

    @Column(name = "billing_address_line2", length = 200)
    private String billingAddressLine2;

    @Column(name = "billing_city", length = 100)
    private String billingCity;

    @Column(name = "billing_state", length = 100)
    private String billingState;

    @Column(name = "billing_state_code", length = 2)
    private String billingStateCode;

    @Column(name = "billing_postal_code", length = 10)
    private String billingPostalCode;

    @Column(name = "shipping_address_line1", length = 200)
    private String shippingAddressLine1;

    @Column(name = "shipping_address_line2", length = 200)
    private String shippingAddressLine2;

    @Column(name = "shipping_city", length = 100)
    private String shippingCity;

    @Column(name = "shipping_state", length = 100)
    private String shippingState;

    @Column(name = "shipping_state_code", length = 2)
    private String shippingStateCode;

    @Column(name = "shipping_postal_code", length = 10)
    private String shippingPostalCode;

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
    @Column(name = "updated_at", nullable = false, insertable = false)
    private Instant updatedAt;

    protected Customer() {
    }

    public static Customer create(CustomerRequest request, UUID createdBy) {
        Customer customer = new Customer();
        customer.apply(request);
        customer.createdBy = createdBy;
        customer.active = true;
        return customer;
    }

    public void update(CustomerRequest request) {
        apply(request);
    }

    private void apply(CustomerRequest request) {
        this.name = request.name().trim();
        this.customerType = request.customerType();
        this.gstin = normalizeGstin(request.gstin());
        this.contactPerson = normalize(request.contactPerson());
        this.phone = normalize(request.phone());
        this.email = normalize(request.email());

        this.billingAddressLine1 = normalize(request.billingAddressLine1());
        this.billingAddressLine2 = normalize(request.billingAddressLine2());
        this.billingCity = normalize(request.billingCity());
        this.billingState = normalize(request.billingState());
        this.billingStateCode = normalize(request.billingStateCode());
        this.billingPostalCode = normalize(request.billingPostalCode());

        this.shippingAddressLine1 = normalize(request.shippingAddressLine1());
        this.shippingAddressLine2 = normalize(request.shippingAddressLine2());
        this.shippingCity = normalize(request.shippingCity());
        this.shippingState = normalize(request.shippingState());
        this.shippingStateCode = normalize(request.shippingStateCode());
        this.shippingPostalCode = normalize(request.shippingPostalCode());

        this.notes = normalize(request.notes());
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeGstin(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase();
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
    public CustomerType getCustomerType() { return customerType; }
    public String getGstin() { return gstin; }
    public String getContactPerson() { return contactPerson; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    public String getBillingAddressLine1() { return billingAddressLine1; }
    public String getBillingAddressLine2() { return billingAddressLine2; }
    public String getBillingCity() { return billingCity; }
    public String getBillingState() { return billingState; }
    public String getBillingStateCode() { return billingStateCode; }
    public String getBillingPostalCode() { return billingPostalCode; }

    public String getShippingAddressLine1() { return shippingAddressLine1; }
    public String getShippingAddressLine2() { return shippingAddressLine2; }
    public String getShippingCity() { return shippingCity; }
    public String getShippingState() { return shippingState; }
    public String getShippingStateCode() { return shippingStateCode; }
    public String getShippingPostalCode() { return shippingPostalCode; }

    public String getNotes() { return notes; }
    public boolean isActive() { return active; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
