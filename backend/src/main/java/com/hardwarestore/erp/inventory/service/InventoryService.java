
package com.hardwarestore.erp.inventory.service;

import com.hardwarestore.erp.auth.repository.AppUserRepository;
import com.hardwarestore.erp.catalogue.entity.Product;
import com.hardwarestore.erp.catalogue.repository.ProductRepository;
import com.hardwarestore.erp.common.exception.ResourceConflictException;
import com.hardwarestore.erp.common.exception.ResourceNotFoundException;
import com.hardwarestore.erp.inventory.dto.*;
import com.hardwarestore.erp.inventory.entity.InventoryBalance;
import com.hardwarestore.erp.inventory.entity.StockMovement;
import com.hardwarestore.erp.inventory.repository.InventoryBalanceRepository;
import com.hardwarestore.erp.inventory.repository.StockMovementRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final AppUserRepository appUserRepository;

    public InventoryService(
            ProductRepository productRepository,
            InventoryBalanceRepository balanceRepository,
            StockMovementRepository movementRepository,
            AppUserRepository appUserRepository
    ) {
        this.productRepository = productRepository;
        this.balanceRepository = balanceRepository;
        this.movementRepository = movementRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public InventoryResponse stockIn(
            StockInRequest request,
            String userId
    ) {
        return processMovement(
                request.productId(),
                request.quantity(),
                "IN",
                request.referenceType(),
                request.referenceId(),
                request.notes(),
                userId
        );
    }

    @Transactional
    public InventoryResponse stockOut(
            StockOutRequest request,
            String userId
    ) {
        return processMovement(
                request.productId(),
                request.quantity().negate(),
                "OUT",
                request.referenceType(),
                request.referenceId(),
                request.notes(),
                userId
        );
    }

    @Transactional
    public InventoryResponse adjust(
            StockAdjustmentRequest request,
            String userId
    ) {
        if (request.quantityChange().compareTo(BigDecimal.ZERO) == 0) {
            throw new ResourceConflictException(
                    "Adjustment quantity cannot be zero"
            );
        }

        return processMovement(
                request.productId(),
                request.quantityChange(),
                "ADJUSTMENT",
                request.referenceType(),
                request.referenceId(),
                request.notes(),
                userId
        );
    }

    @Transactional
    public InventoryResponse receivePurchaseGoods(
            UUID productId,
            BigDecimal quantity,
            UUID goodsReceiptId,
            String notes,
            String userId
    ) {
        return processMovement(
                productId,
                quantity,
                "IN",
                "GOODS_RECEIPT",
                goodsReceiptId,
                notes,
                userId
        );
    }

    private InventoryResponse processMovement(
            UUID productId,
            BigDecimal change,
            String movementType,
            String referenceType,
            UUID referenceId,
            String notes,
            String userId
    ) {
        Product product = productRepository
                .findActiveByIdForUpdate(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active product not found: " + productId
                        )
                );

        int scale = product.getUom().getQuantityScale();

        try {
            change = change.setScale(
                    scale,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new ResourceConflictException(
                    "Quantity exceeds the precision allowed for this unit"
            );
        }

        if (change.compareTo(BigDecimal.ZERO) == 0) {
            throw new ResourceConflictException(
                    "Quantity cannot be zero"
            );
        }

        InventoryBalance balance = balanceRepository
                .findByProductId(productId)
                .orElseGet(() ->
                        balanceRepository.save(
                                InventoryBalance.create(product)
                        )
                );

        try {
            balance.applyChange(change);
        } catch (IllegalArgumentException exception) {
            throw new ResourceConflictException(
                    exception.getMessage()
            );
        }

        UUID createdBy = appUserRepository
                .findById(UUID.fromString(userId))
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                )
                .getId();

        StockMovement movement = StockMovement.create(
                product,
                movementType,
                change,
                referenceType,
                referenceId,
                notes,
                createdBy
        );

        movementRepository.save(movement);

        balanceRepository.flush();
        movementRepository.flush();

        return toInventoryResponse(balance);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventory(UUID productId) {
        InventoryBalance balance = balanceRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found for product: " + productId
                        )
                );

        return toInventoryResponse(balance);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getMovements(
            UUID productId,
            Pageable pageable
    ) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }

        return movementRepository
                .findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(this::toMovementResponse);
    }

    private InventoryResponse toInventoryResponse(
            InventoryBalance balance
    ) {
        Product product = balance.getProduct();

        BigDecimal available = balance.getQuantityOnHand()
                .subtract(balance.getQuantityReserved());

        return new InventoryResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getUom().getCode(),
                balance.getQuantityOnHand(),
                balance.getQuantityReserved(),
                available,
                product.getReorderLevel(),
                available.compareTo(product.getReorderLevel()) <= 0,
                balance.getUpdatedAt()
        );
    }

    private StockMovementResponse toMovementResponse(
            StockMovement movement
    ) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getSku(),
                movement.getMovementType(),
                movement.getQuantityChange(),
                movement.getReferenceType(),
                movement.getReferenceId(),
                movement.getNotes(),
                movement.getCreatedBy(),
                movement.getCreatedAt()
        );
    }
}
