package com.hardwarestore.erp.catalogue.service;

import com.hardwarestore.erp.catalogue.dto.GstRateResponse;
import com.hardwarestore.erp.catalogue.entity.GstRate;
import com.hardwarestore.erp.catalogue.repository.GstRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GstRateService {

    private final GstRateRepository gstRateRepository;

    public GstRateService(GstRateRepository gstRateRepository) {
        this.gstRateRepository = gstRateRepository;
    }

    @Transactional(readOnly = true)
    public List<GstRateResponse> getAllActiveRates() {

        return gstRateRepository
                .findAllByActiveTrueOrderByGstRateAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private GstRateResponse toResponse(GstRate gstRate) {

        return new GstRateResponse(
                gstRate.getId(),
                gstRate.getCode(),
                gstRate.getDescription(),
                gstRate.getGstRate(),
                gstRate.getEffectiveFrom(),
                gstRate.getEffectiveTo()
        );
    }
}
