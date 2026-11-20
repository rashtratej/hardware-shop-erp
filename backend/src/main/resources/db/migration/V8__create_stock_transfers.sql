-- V8: Create stock transfer headers and lines.

CREATE TABLE stock_transfers (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                 transfer_number VARCHAR(40) NOT NULL UNIQUE,

                                 source_warehouse_id UUID NOT NULL
                                     REFERENCES warehouses(id) ON DELETE RESTRICT,

                                 destination_warehouse_id UUID NOT NULL
                                     REFERENCES warehouses(id) ON DELETE RESTRICT,

                                 status VARCHAR(16) NOT NULL
                                     CHECK (
                                         status IN (
                                                    'DRAFT',
                                                    'COMPLETED',
                                                    'CANCELLED',
                                                    'REVERSED'
                                             )
                                         ),

                                 note TEXT,

                                 created_by UUID NOT NULL
                                     REFERENCES app_users(id) ON DELETE RESTRICT,

                                 completed_by UUID
                                     REFERENCES app_users(id) ON DELETE RESTRICT,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 completed_at TIMESTAMPTZ,

                                 CONSTRAINT chk_stock_transfers_different_warehouses
                                     CHECK (source_warehouse_id <> destination_warehouse_id),

                                 CONSTRAINT chk_stock_transfers_completed_metadata
                                     CHECK (
                                         status <> 'COMPLETED'
                                             OR (completed_by IS NOT NULL AND completed_at IS NOT NULL)
                                         )
);

CREATE INDEX idx_stock_transfers_source_created
    ON stock_transfers (source_warehouse_id, created_at DESC);

CREATE INDEX idx_stock_transfers_destination_created
    ON stock_transfers (destination_warehouse_id, created_at DESC);


CREATE TABLE stock_transfer_lines (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                      transfer_id UUID NOT NULL
                                          REFERENCES stock_transfers(id) ON DELETE RESTRICT,

                                      product_id UUID NOT NULL
                                          REFERENCES products(id) ON DELETE RESTRICT,

                                      quantity NUMERIC(14,3) NOT NULL
                                          CHECK (quantity > 0),

                                      unit_cost_snapshot NUMERIC(14,4) NOT NULL
                                          CHECK (unit_cost_snapshot >= 0),

                                      created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_transfer_lines_transfer_id
    ON stock_transfer_lines (transfer_id);

CREATE INDEX idx_stock_transfer_lines_product_id
    ON stock_transfer_lines (product_id);