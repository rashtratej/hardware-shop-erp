
package com.hardwarestore.erp.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record GoodsReceiptRequest(

        @NotNull
        LocalDate receiptDate,

        @Size(max = 100)
        String supplierDeliveryNote,

        @Size(max = 1000)
        String notes,

        @NotEmpty
        @Size(max = 1000)
        List<@Valid GoodsReceiptItemRequest> items

) {}
