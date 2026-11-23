
package com.hardwarestore.erp.purchase.service;

import com.hardwarestore.erp.auth.repository.AppUserRepository;
import com.hardwarestore.erp.common.exception.ResourceConflictException;
import com.hardwarestore.erp.common.exception.ResourceNotFoundException;
import com.hardwarestore.erp.inventory.service.InventoryService;
import com.hardwarestore.erp.purchase.dto.*;
import com.hardwarestore.erp.purchase.entity.*;
import com.hardwarestore.erp.purchase.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GoodsReceiptService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptItemRepository goodsReceiptItemRepository;
    private final InventoryService inventoryService;
    private final AppUserRepository appUserRepository;

    public GoodsReceiptService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            GoodsReceiptRepository goodsReceiptRepository,
            GoodsReceiptItemRepository goodsReceiptItemRepository,
            InventoryService inventoryService,
            AppUserRepository appUserRepository
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptItemRepository = goodsReceiptItemRepository;
        this.inventoryService = inventoryService;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public GoodsReceiptResponse create(
            UUID purchaseOrderId,
            GoodsReceiptRequest request,
            String userId
    ) {
        UUID authenticatedUserId = getAuthenticatedUserId(userId);

        // Lock the PO first. Concurrent receipts against this PO
        // must wait until the current receipt transaction completes.
        PurchaseOrder order = purchaseOrderRepository
                .findByIdForUpdate(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase order not found: " + purchaseOrderId
                ));

        if (order.getStatus() != PurchaseOrderStatus.APPROVED
                && order.getStatus() != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new ResourceConflictException(
                    "Goods can only be received against approved purchase orders"
            );
        }

        List<PurchaseOrderItem> orderItems =
                purchaseOrderItemRepository.findByPurchaseOrderId(order.getId());

        if (orderItems.isEmpty()) {
            throw new ResourceConflictException(
                    "Purchase order has no items"
            );
        }

        Map<UUID, PurchaseOrderItem> itemMap = new HashMap<>();
        for (PurchaseOrderItem item : orderItems) {
            itemMap.put(item.getId(), item);
        }

        Set<UUID> requestedItemIds = new HashSet<>();

        // Resolve and validate all lines before changing inventory.
        List<PreparedReceiptItem> preparedItems = new ArrayList<>();

        for (GoodsReceiptItemRequest itemRequest : request.items()) {
            UUID orderItemId = itemRequest.purchaseOrderItemId();

            if (!requestedItemIds.add(orderItemId)) {
                throw new ResourceConflictException(
                        "Duplicate purchase order item in goods receipt"
                );
            }

            PurchaseOrderItem orderItem = itemMap.get(orderItemId);

            if (orderItem == null) {
                throw new ResourceConflictException(
                        "Goods receipt item does not belong to this purchase order"
                );
            }

            if (itemRequest.receivedQuantity() == null
                    || itemRequest.receivedQuantity()
                    .compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new ResourceConflictException(
                        "Received quantity must be greater than zero"
                );
            }

            if (itemRequest.receivedQuantity()
                    .compareTo(orderItem.getOutstandingQuantity()) > 0) {
                throw new ResourceConflictException(
                        "Received quantity cannot exceed outstanding quantity"
                );
            }

            preparedItems.add(new PreparedReceiptItem(
                    orderItem,
                    itemRequest.receivedQuantity()
            ));
        }

        // Stable product lock order reduces deadlock risk when
        // different POs receive overlapping products concurrently.
        preparedItems.sort(
                Comparator.comparing(
                        item -> item.orderItem().getProduct().getId()
                )
        );

        GoodsReceipt receipt = GoodsReceipt.create(
                order,
                request.receiptDate(),
                normalize(request.supplierDeliveryNote()),
                normalize(request.notes()),
                authenticatedUserId
        );

        // Flush to obtain the database-generated receipt number and ID.
        GoodsReceipt savedReceipt =
                goodsReceiptRepository.saveAndFlush(receipt);

        List<GoodsReceiptItem> receiptItems = new ArrayList<>();

        for (PreparedReceiptItem prepared : preparedItems) {
            PurchaseOrderItem orderItem = prepared.orderItem();
            java.math.BigDecimal quantity = prepared.quantity();

            orderItem.recordReceivedQuantity(quantity);

            GoodsReceiptItem receiptItem = GoodsReceiptItem.create(
                    savedReceipt,
                    orderItem,
                    quantity
            );

            receiptItems.add(receiptItem);
        }

        goodsReceiptItemRepository.saveAllAndFlush(receiptItems);

        // Update inventory and write the stock ledger for every line.
        // These operations participate in this same transaction.
        String movementNotes = "Goods receipt " +
                savedReceipt.getReceiptNumber();

        for (PreparedReceiptItem prepared : preparedItems) {
            inventoryService.receivePurchaseGoods(
                    prepared.orderItem().getProduct().getId(),
                    prepared.quantity(),
                    savedReceipt.getId(),
                    movementNotes,
                    userId
            );
        }

        boolean fullyReceived = orderItems.stream()
                .allMatch(item ->
                        item.getOutstandingQuantity()
                                .compareTo(java.math.BigDecimal.ZERO) == 0
                );

        if (fullyReceived) {
            order.markReceived();
        } else {
            order.markPartiallyReceived();
        }

        purchaseOrderRepository.flush();

        return toResponse(savedReceipt, receiptItems);
    }

    @Transactional(readOnly = true)
    public List<GoodsReceiptResponse> getByPurchaseOrder(
            UUID purchaseOrderId
    ) {
        if (!purchaseOrderRepository.existsById(purchaseOrderId)) {
            throw new ResourceNotFoundException(
                    "Purchase order not found: " + purchaseOrderId
            );
        }

        return goodsReceiptRepository
                .findByPurchaseOrderIdOrderByReceiptDateDesc(purchaseOrderId)
                .stream()
                .map(receipt -> toResponse(
                        receipt,
                        goodsReceiptItemRepository.findByGoodsReceiptId(
                                receipt.getId()
                        )
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public GoodsReceiptResponse getById(UUID receiptId) {
        GoodsReceipt receipt = goodsReceiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Goods receipt not found: " + receiptId
                ));

        return toResponse(
                receipt,
                goodsReceiptItemRepository.findByGoodsReceiptId(receiptId)
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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found"
                ))
                .getId();
    }

    private GoodsReceiptResponse toResponse(
            GoodsReceipt receipt,
            List<GoodsReceiptItem> items
    ) {
        List<GoodsReceiptItemResponse> itemResponses = items.stream()
                .map(item -> {
                    PurchaseOrderItem orderItem =
                            item.getPurchaseOrderItem();

                    return new GoodsReceiptItemResponse(
                            item.getId(),
                            orderItem.getId(),
                            item.getProduct().getId(),
                            orderItem.getSkuSnapshot(),
                            orderItem.getProductNameSnapshot(),
                            orderItem.getUomCodeSnapshot(),
                            item.getReceivedQuantity()
                    );
                })
                .toList();

        return new GoodsReceiptResponse(
                receipt.getId(),
                receipt.getReceiptNumber(),
                receipt.getPurchaseOrder().getId(),
                receipt.getPurchaseOrder().getPoNumber(),
                receipt.getReceiptDate(),
                receipt.getSupplierDeliveryNote(),
                receipt.getNotes(),
                receipt.getReceivedBy(),
                receipt.getCreatedAt(),
                itemResponses
        );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record PreparedReceiptItem(
            PurchaseOrderItem orderItem,
            java.math.BigDecimal quantity
    ) {}
}
