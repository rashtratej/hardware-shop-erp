
package com.hardwarestore.erp.catalogue.repository;

import com.hardwarestore.erp.catalogue.entity.UnitOfMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UnitOfMeasurementRepository
        extends JpaRepository<UnitOfMeasurement, UUID> {

    List<UnitOfMeasurement> findAllByActiveTrueOrderByDisplayNameAsc();

    boolean existsByCodeIgnoreCase(String code);
}