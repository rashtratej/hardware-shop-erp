package com.hardwarestore.erp.purchase.service;

import com.hardwarestore.erp.auth.repository.AppUserRepository;
import com.hardwarestore.erp.catalogue.entity.GstRate;
import com.hardwarestore.erp.catalogue.entity.Product;
import com.hardwarestore.erp.catalogue.repository.ProductRepository;
import com.hardwarestore.erp.common.exception.ResourceConflictException;
import com.hardwarestore.erp.common.exception.ResourceNotFoundException;
import com.hardwarestore.erp.purchase.dto.PurchaseOrderItemRequest;
import com.hardwarestore.erp.purchase.dto.PurchaseOrderItemResponse;
import com.hardwarestore.erp.purchase.dto.PurchaseOrderRequest;
import com.hardwarestore.erp.purchase.dto.PurchaseOrderResponse;
import com.hardwarestore.erp.purchase.entity.PurchaseOrder;
import com.hardwarestore.erp.purchase.entity.PurchaseOrderItem;
import com.hardwarestore.erp.purchase.entity.PurchaseOrderStatus;
import com.hardwarestore.erp.purchase.repository.PurchaseOrderItemRepository;
import com.hardwarestore.erp.purchase.repository.PurchaseOrderRepository;
import com.hardwarestore.erp.supplier.entity.Supplier;
import com.hardwarestore.erp.supplier.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PurchaseOrderService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private static final int MONEY_SCALE = 2;

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository itemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final AppUserRepository appUserRepository;

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository itemRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            AppUserRepository appUserRepository
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.itemRepository = itemRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public PurchaseOrderResponse create(
            PurchaseOrderRequest request,
            String userId
    ) {
        validateDates(request);
        UUID authenticatedUserId = getAuthenticatedUserId(userId);
        Supplier supplier = getActiveSupplier(request.supplierId());

        PurchaseOrder order = PurchaseOrder.create(
                supplier,
                request.orderDate(),
                request.expectedDeliveryDate(),
                normalizeNotes(request.notes()),
                authenticatedUserId
        );

        PreparedItems prepared = prepareItems(order, request);
        order.updateTotals(
                prepared.subtotal(),
                prepared.taxTotal(),
                prepared.grandTotal()
        );

        PurchaseOrder savedOrder =
                purchaseOrderRepository.saveAndFlush(order);

        List<PurchaseOrderItem> savedItems =
                itemRepository.saveAllAndFlush(prepared.items());

        return toResponse(savedOrder, savedItems);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> search(
            PurchaseOrderStatus status,
            UUID supplierId,
            Pageable pageable
    ) {
        return purchaseOrderRepository
                .search(status, supplierId, pageable)
                .map(order -> toResponse(
                        order,
                        itemRepository.findByPurchaseOrderId(order.getId())
                ));
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getById(UUID id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Purchase order not found: " + id
                        )
                );

        List<PurchaseOrderItem> items =
                itemRepository.findByPurchaseOrderId(order.getId());

        return toResponse(order, items);
    }

    @Transactional
    public PurchaseOrderResponse updateDraft(
            UUID id,
            PurchaseOrderRequest request,
            String userId
    ) {
        validateDates(request);
        getAuthenticatedUserId(userId);

        PurchaseOrder order = purchaseOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Purchase order not found: " + id
                        )
                );

        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new ResourceConflictException(
                    "Only draft purchase orders can be updated"
            );
        }

        Supplier supplier = getActiveSupplier(request.supplierId());

        order.updateDraftDetails(
                supplier,
                request.orderDate(),
                request.expectedDeliveryDate(),
                normalizeNotes(request.notes())
        );

        PreparedItems prepared = prepareItems(order, request);

        order.updateTotals(
                prepared.subtotal(),
                prepared.taxTotal(),
                prepared.grandTotal()
        );

        List<PurchaseOrderItem> existingItems =
                itemRepository.findByPurchaseOrderId(order.getId());

        itemRepository.deleteAll(existingItems);
        itemRepository.flush();

        List<PurchaseOrderItem> savedItems =
                itemRepository.saveAllAndFlush(prepared.items());

        purchaseOrderRepository.flush();

        return toResponse(order, savedItems);
    }

    @Transactional
    public PurchaseOrderResponse submitForApproval(UUID id) {
        PurchaseOrder order = getOrderForUpdate(id);

        if (itemRepository.findByPurchaseOrderId(id).isEmpty()) {
            throw new ResourceConflictException(
                    "Purchase order must contain at least one item"
            );
        }

        try {
            order.submitForApproval();
        } catch (IllegalStateException exception) {
            throw new ResourceConflictException(exception.getMessage());
        }

        return toResponse(
                order,
                itemRepository.findByPurchaseOrderId(id)
        );
    }

    @Transactional
    public PurchaseOrderResponse approve(UUID id, String userId) {
        UUID authenticatedUserId = getAuthenticatedUserId(userId);
        PurchaseOrder order = getOrderForUpdate(id);

        try {
            order.approve(authenticatedUserId);
        } catch (IllegalStateException exception) {
            throw new ResourceConflictException(exception.getMessage());
        }

        return toResponse(
                order,
                itemRepository.findByPurchaseOrderId(id)
        );
    }

    @Transactional
    public PurchaseOrderResponse reject(
            UUID id,
            String userId,
            String reason
    ) {
        UUID authenticatedUserId = getAuthenticatedUserId(userId);
        PurchaseOrder order = getOrderForUpdate(id);

        try {
            order.reject(authenticatedUserId, reason.trim());
        } catch (IllegalStateException exception) {
            throw new ResourceConflictException(exception.getMessage());
        }

        return toResponse(
                order,
                itemRepository.findByPurchaseOrderId(id)
        );
    }

    @Transactional
    public PurchaseOrderResponse cancel(
            UUID id,
            String userId,
            String reason
    ) {
        UUID authenticatedUserId = getAuthenticatedUserId(userId);
        PurchaseOrder order = getOrderForUpdate(id);

        try {
            order.cancel(authenticatedUserId, reason.trim());
        } catch (IllegalStateException exception) {
            throw new ResourceConflictException(exception.getMessage());
        }

        return toResponse(
                order,
                itemRepository.findByPurchaseOrderId(id)
        );
    }

    private PurchaseOrder getOrderForUpdate(UUID id) {
        return purchaseOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Purchase order not found: " + id
                        )
                );
    }

    private PreparedItems prepareItems(
            PurchaseOrder order,
            PurchaseOrderRequest request
    ) {
        Set<UUID> productIds = new HashSet<>();

        BigDecimal subtotal = BigDecimal.ZERO.setScale(MONEY_SCALE);
        BigDecimal taxTotal = BigDecimal.ZERO.setScale(MONEY_SCALE);

        List<PurchaseOrderItem> items = new ArrayList<>();

        for (PurchaseOrderItemRequest itemRequest : request.items()) {

            if (!productIds.add(itemRequest.productId())) {
                throw new ResourceConflictException(
                        "Duplicate product in purchase order: "
                                + itemRequest.productId()
                );
            }

            Product product = productRepository
                    .findByIdAndActiveTrue(itemRequest.productId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Active product not found: "
                                            + itemRequest.productId()
                            )
                    );

            validateQuantity(itemRequest, product);

            BigDecimal unitCost = normalizeUnitCost(
                    itemRequest.unitCost()
            );

            GstRate gstRate = product.getGstRate();
            validateGstRate(gstRate, request.orderDate());

            BigDecimal rate = gstRate.getGstRate();

            BigDecimal taxableAmount = itemRequest.orderedQuantity()
                    .multiply(unitCost)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal taxAmount = taxableAmount
                    .multiply(rate)
                    .divide(
                            ONE_HUNDRED,
                            MONEY_SCALE,
                            RoundingMode.HALF_UP
                    );

            BigDecimal lineTotal = taxableAmount.add(taxAmount)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            PurchaseOrderItem item = PurchaseOrderItem.create(
                    order,
                    product,
                    itemRequest.orderedQuantity(),
                    unitCost,
                    rate,
                    taxableAmount,
                    taxAmount,
                    lineTotal
            );

            items.add(item);
            subtotal = subtotal.add(taxableAmount);
            taxTotal = taxTotal.add(taxAmount);
        }

        BigDecimal grandTotal = subtotal.add(taxTotal)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        return new PreparedItems(items, subtotal, taxTotal, grandTotal);
    }

    private Supplier getActiveSupplier(UUID supplierId) {
        return supplierRepository.findByIdAndActiveTrue(supplierId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active supplier not found: " + supplierId
                        )
                );
    }

    private void validateDates(PurchaseOrderRequest request) {
        if (request.expectedDeliveryDate() != null
                && request.expectedDeliveryDate()
                .isBefore(request.orderDate())) {
            throw new ResourceConflictException(
                    "Expected delivery date cannot be before order date"
            );
        }
    }

    private void validateQuantity(
            PurchaseOrderItemRequest request,
            Product product
    ) {
        if (!product.getUom().isActive()) {
            throw new ResourceConflictException(
                    "Unit of measurement is inactive for product: "
                            + product.getName()
            );
        }

        try {
            request.orderedQuantity().setScale(
                    product.getUom().getQuantityScale(),
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new ResourceConflictException(
                    "Quantity exceeds the precision allowed for product: "
                            + product.getName()
            );
        }
    }

    private BigDecimal normalizeUnitCost(BigDecimal unitCost) {
        try {
            return unitCost.setScale(
                    MONEY_SCALE,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new ResourceConflictException(
                    "Unit cost supports a maximum of 2 decimal places"
            );
        }
    }

    private void validateGstRate(
            GstRate gstRate,
            LocalDate orderDate
    ) {
        if (!gstRate.isActive()) {
            throw new ResourceConflictException("GST rate is inactive");
        }

        if (gstRate.getEffectiveFrom().isAfter(orderDate)) {
            throw new ResourceConflictException(
                    "GST rate is not effective on the purchase order date"
            );
        }

        if (gstRate.getEffectiveTo() != null
                && gstRate.getEffectiveTo().isBefore(orderDate)) {
            throw new ResourceConflictException(
                    "GST rate has expired for the purchase order date"
            );
        }
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

    private String normalizeNotes(String notes) {
        return notes == null || notes.isBlank()
                ? null
                : notes.trim();
    }

    private PurchaseOrderResponse toResponse(
            PurchaseOrder order,
            List<PurchaseOrderItem> items
    ) {
        List<PurchaseOrderItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();

        return new PurchaseOrderResponse(
                order.getId(),
                order.getPoNumber(),
                order.getSupplier().getId(),
                order.getSupplier().getName(),
                order.getOrderDate(),
                order.getExpectedDeliveryDate(),
                order.getStatus(),
                order.getSubtotal(),
                order.getTaxTotal(),
                order.getGrandTotal(),
                order.getNotes(),
                order.getCreatedBy(),
                order.getCreatedAt(),
                itemResponses
        );
    }

    private PurchaseOrderItemResponse toItemResponse(
            PurchaseOrderItem item
    ) {
        return new PurchaseOrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getSkuSnapshot(),
                item.getProductNameSnapshot(),
                item.getHsnCodeSnapshot(),
                item.getUomCodeSnapshot(),
                item.getOrderedQuantity(),
                item.getReceivedQuantity(),
                item.getOutstandingQuantity(),
                item.getUnitCost(),
                item.getGstRate(),
                item.getTaxableAmount(),
                item.getTaxAmount(),
                item.getLineTotal()
        );
    }

    private record PreparedItems(
            List<PurchaseOrderItem> items,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal grandTotal
    ) {
    }
}