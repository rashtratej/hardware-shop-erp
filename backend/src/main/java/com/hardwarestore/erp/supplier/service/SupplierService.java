
package com.hardwarestore.erp.supplier.service;

import com.hardwarestore.erp.auth.repository.AppUserRepository;
import com.hardwarestore.erp.common.exception.ResourceConflictException;
import com.hardwarestore.erp.common.exception.ResourceNotFoundException;
import com.hardwarestore.erp.supplier.dto.SupplierRequest;
import com.hardwarestore.erp.supplier.dto.SupplierResponse;
import com.hardwarestore.erp.supplier.entity.Supplier;
import com.hardwarestore.erp.supplier.repository.SupplierRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AppUserRepository appUserRepository;

    public SupplierService(
            SupplierRepository supplierRepository,
            AppUserRepository appUserRepository
    ) {
        this.supplierRepository = supplierRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public SupplierResponse create(
            SupplierRequest request,
            String userId
    ) {
        String gstin = normalizeGstin(request.gstin());

        if (gstin != null && supplierRepository.existsByGstin(gstin)) {
            throw new ResourceConflictException(
                    "A supplier with this GSTIN already exists"
            );
        }

        UUID createdBy = getAuthenticatedUserId(userId);

        Supplier supplier = Supplier.create(request, createdBy);

        return saveSupplier(supplier);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found: " + id
                        )
                );

        return toResponse(supplier);
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> search(
            String search,
            Boolean active,
            Pageable pageable
    ) {
        String normalizedSearch =
                search == null || search.isBlank() ? "" : search.trim();

        return supplierRepository
                .search(normalizedSearch, active, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public SupplierResponse update(
            UUID id,
            SupplierRequest request
    ) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found: " + id
                        )
                );

        String gstin = normalizeGstin(request.gstin());

        if (gstin != null
                && supplierRepository.existsByGstinAndIdNot(gstin, id)) {
            throw new ResourceConflictException(
                    "A supplier with this GSTIN already exists"
            );
        }

        supplier.update(request);

        return saveSupplier(supplier);
    }

    @Transactional
    public SupplierResponse deactivate(UUID id) {
        Supplier supplier = getSupplier(id);
        supplier.deactivate();
        return saveSupplier(supplier);
    }

    @Transactional
    public SupplierResponse activate(UUID id) {
        Supplier supplier = getSupplier(id);
        supplier.activate();
        return saveSupplier(supplier);
    }

    private Supplier getSupplier(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found: " + id
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

    private SupplierResponse saveSupplier(Supplier supplier) {
        try {
            return toResponse(supplierRepository.saveAndFlush(supplier));
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceConflictException(
                    "A supplier with this GSTIN already exists"
            );
        }
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getGstin(),
                supplier.getContactPerson(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddressLine1(),
                supplier.getAddressLine2(),
                supplier.getCity(),
                supplier.getState(),
                supplier.getStateCode(),
                supplier.getPostalCode(),
                supplier.getNotes(),
                supplier.isActive(),
                supplier.getCreatedBy(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }
}
