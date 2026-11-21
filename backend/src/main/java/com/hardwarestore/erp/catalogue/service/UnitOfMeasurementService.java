package com.hardwarestore.erp.catalogue.service;

import com.hardwarestore.erp.catalogue.dto.UnitOfMeasurementResponse;
import com.hardwarestore.erp.catalogue.entity.UnitOfMeasurement;
import com.hardwarestore.erp.catalogue.repository.UnitOfMeasurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UnitOfMeasurementService {

    private final UnitOfMeasurementRepository uomRepository;

    public UnitOfMeasurementService(
            UnitOfMeasurementRepository uomRepository
    ) {
        this.uomRepository = uomRepository;
    }

    @Transactional(readOnly = true)
    public List<UnitOfMeasurementResponse> getAllActiveUnits() {

        return uomRepository.findAllByActiveTrueOrderByDisplayNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UnitOfMeasurementResponse toResponse(
            UnitOfMeasurement uom
    ) {
        return new UnitOfMeasurementResponse(
                uom.getId(),
                uom.getCode(),
                uom.getDisplayName(),
                uom.getQuantityScale()
        );
    }
}
