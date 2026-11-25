package com.hardwarestore.erp.customer.service;

import com.hardwarestore.erp.auth.repository.AppUserRepository;
import com.hardwarestore.erp.common.exception.ResourceConflictException;
import com.hardwarestore.erp.common.exception.ResourceNotFoundException;
import com.hardwarestore.erp.customer.dto.CustomerRequest;
import com.hardwarestore.erp.customer.dto.CustomerResponse;
import com.hardwarestore.erp.customer.entity.Customer;
import com.hardwarestore.erp.customer.entity.CustomerType;
import com.hardwarestore.erp.customer.repository.CustomerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AppUserRepository appUserRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            AppUserRepository appUserRepository
    ) {
        this.customerRepository = customerRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request, String userId) {
        String gstin = normalizeGstin(request.gstin());

        if (gstin != null && customerRepository.existsByGstin(gstin)) {
            throw new ResourceConflictException(
                    "A customer with this GSTIN already exists"
            );
        }

        UUID createdBy = getAuthenticatedUserId(userId);
        Customer customer = Customer.create(request, createdBy);

        return saveCustomer(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(UUID id) {
        return toResponse(getCustomer(id));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> search(
            String search,
            Boolean active,
            CustomerType customerType,
            Pageable pageable
    ) {
        String normalizedSearch =
                search == null || search.isBlank() ? "" : search.trim();

        return customerRepository
                .search(normalizedSearch, active, customerType, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = getCustomer(id);
        String gstin = normalizeGstin(request.gstin());

        if (gstin != null
                && customerRepository.existsByGstinAndIdNot(gstin, id)) {
            throw new ResourceConflictException(
                    "A customer with this GSTIN already exists"
            );
        }

        customer.update(request);
        return saveCustomer(customer);
    }

    @Transactional
    public CustomerResponse deactivate(UUID id) {
        Customer customer = getCustomer(id);
        customer.deactivate();
        return saveCustomer(customer);
    }

    @Transactional
    public CustomerResponse activate(UUID id) {
        Customer customer = getCustomer(id);
        customer.activate();
        return saveCustomer(customer);
    }

    private Customer getCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + id
                        )
                );
    }

    private UUID getAuthenticatedUserId(String userId) {
        UUID id;

        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException exception) {
            throw new ResourceNotFoundException(
                    "Authenticated user not found"
            );
        }

        return appUserRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                )
                .getId();
    }

    private String normalizeGstin(String gstin) {
        return gstin == null || gstin.isBlank()
                ? null
                : gstin.trim().toUpperCase();
    }

    private CustomerResponse saveCustomer(Customer customer) {
        try {
            return toResponse(customerRepository.saveAndFlush(customer));
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceConflictException(
                    "Customer data conflicts with an existing record"
            );
        }
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getCustomerType(),
                customer.getGstin(),
                customer.getContactPerson(),
                customer.getPhone(),
                customer.getEmail(),

                customer.getBillingAddressLine1(),
                customer.getBillingAddressLine2(),
                customer.getBillingCity(),
                customer.getBillingState(),
                customer.getBillingStateCode(),
                customer.getBillingPostalCode(),

                customer.getShippingAddressLine1(),
                customer.getShippingAddressLine2(),
                customer.getShippingCity(),
                customer.getShippingState(),
                customer.getShippingStateCode(),
                customer.getShippingPostalCode(),

                customer.getNotes(),
                customer.isActive(),
                customer.getCreatedBy(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
