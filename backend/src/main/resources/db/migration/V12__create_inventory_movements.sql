
-- V12: Create the immutable inventory movement ledger.

CREATE TABLE inventory_movements (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                     warehouse_id UUID NOT NULL
                                         REFERENCES warehouses(id) ON DELETE RESTRICT,

                                     product_id UUID NOT NULL
                                         REFERENCES products(id) ON DELETE RESTRICT,

                                     movement_type VARCHAR(24) NOT NULL
                                         CHECK (
                                             movement_type IN (
                                                               'RECEIPT',
                                                               'SALE',
                                                               'ADJUSTMENT',
                                                               'TRANSFER_OUT',
                                                               'TRANSFER_IN',
                                                               'SALE_VOID_REVERSAL',
                                                               'TRANSFER_REVERSAL'
                                                 )
                                             ),

                                     quantity_delta NUMERIC(14,3) NOT NULL
                                         CHECK (quantity_delta <> 0),

                                     unit_cost NUMERIC(14,4) NOT NULL
                                         CHECK (unit_cost >= 0),

                                     receipt_line_id UUID
                                         REFERENCES stock_receipt_lines(id) ON DELETE RESTRICT,

                                     adjustment_line_id UUID
                                         REFERENCES stock_adjustment_lines(id) ON DELETE RESTRICT,

                                     transfer_line_id UUID
                                         REFERENCES stock_transfer_lines(id) ON DELETE RESTRICT,

                                     invoice_line_id UUID
                                         REFERENCES invoice_lines(id) ON DELETE RESTRICT,

                                     idempotency_key UUID NOT NULL UNIQUE,

                                     created_by UUID NOT NULL
                                         REFERENCES app_users(id) ON DELETE RESTRICT,

                                     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Exactly one originating document line must be referenced.
                                     CONSTRAINT chk_inventory_movements_exactly_one_source
                                         CHECK (
                                             num_nonnulls(
                                                     receipt_line_id,
                                                     adjustment_line_id,
                                                     transfer_line_id,
                                                     invoice_line_id
                                             ) = 1
                                             ),

    -- Ensure the source-line reference matches the movement type.
                                     CONSTRAINT chk_inventory_movements_source_type
                                         CHECK (
                                             (
                                                 movement_type = 'RECEIPT'
                                                     AND receipt_line_id IS NOT NULL
                                                 )
                                                 OR
                                             (
                                                 movement_type = 'ADJUSTMENT'
                                                     AND adjustment_line_id IS NOT NULL
                                                 )
                                                 OR
                                             (
                                                 movement_type IN (
                                                                   'TRANSFER_OUT',
                                                                   'TRANSFER_IN',
                                                                   'TRANSFER_REVERSAL'
                                                     )
                                                     AND transfer_line_id IS NOT NULL
                                                 )
                                                 OR
                                             (
                                                 movement_type IN ('SALE', 'SALE_VOID_REVERSAL')
                                                     AND invoice_line_id IS NOT NULL
                                                 )
                                             )
);

-- Supports product stock history by warehouse and time.
CREATE INDEX idx_inventory_movements_product_warehouse_created
    ON inventory_movements (product_id, warehouse_id, created_at);

-- Prevent accidental modification or deletion of ledger records.
CREATE FUNCTION prevent_inventory_movement_mutation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION
        'Inventory movements are immutable; create a reversal instead.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_inventory_movements_immutable
    BEFORE UPDATE OR DELETE ON inventory_movements
    FOR EACH ROW
    EXECUTE FUNCTION prevent_inventory_movement_mutation();