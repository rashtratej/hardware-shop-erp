package com.hardwarestore.erp.inventory.repository;

import com.hardwarestore.erp.inventory.entity.InventoryBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryBalanceRepository
        extends JpaRepository<InventoryBalance, UUID> {

    Optional<InventoryBalance> findByProductId(UUID productId);
}