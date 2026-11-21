
package com.hardwarestore.erp.catalogue.repository;

import com.hardwarestore.erp.catalogue.entity.GstRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GstRateRepository
        extends JpaRepository<GstRate, UUID> {

    List<GstRate> findAllByActiveTrueOrderByGstRateAsc();

}
