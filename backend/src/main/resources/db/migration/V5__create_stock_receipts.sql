
-- V5: Create stock receipt headers and lines.

CREATE TABLE stock_receipts (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                receipt_number VARCHAR(40) NOT NULL UNIQUE,

                                warehouse_id UUID NOT NULL
                                    REFERENCES warehouses(id) ON DELETE RESTRICT,

                                supplier_name VARCHAR(160),
                                supplier_gstin CHAR(15),
                                reference VARCHAR(80),

                                received_at TIMESTAMPTZ NOT NULL,

                                status VARCHAR(16) NOT NULL
                                    CHECK (status IN ('DRAFT', 'POSTED', 'VOID')),

                                created_by UUID NOT NULL
                                    REFERENCES app_users(id) ON DELETE RESTRICT,

                                posted_by UUID
                                    REFERENCES app_users(id) ON DELETE RESTRICT,

                                posted_at TIMESTAMPTZ,

                                created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT chk_stock_receipts_posted_metadata
                                    CHECK (
                                        status <> 'POSTED'
                                            OR (posted_by IS NOT NULL AND posted_at IS NOT NULL)
                                        )
);

CREATE INDEX idx_stock_receipts_warehouse_received
    ON stock_receipts (warehouse_id, received_at DESC);

CREATE TABLE stock_receipt_lines (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                     receipt_id UUID NOT NULL
                                         REFERENCES stock_receipts(id) ON DELETE RESTRICT,

                                     product_id UUID NOT NULL
                                         REFERENCES products(id) ON DELETE RESTRICT,

                                     quantity NUMERIC(14,3) NOT NULL
                                         CHECK (quantity > 0),

                                     unit_cost_ex_gst NUMERIC(14,4) NOT NULL
                                         CHECK (unit_cost_ex_gst >= 0),

                                     line_reference VARCHAR(80),

                                     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_receipt_lines_receipt_id
    ON stock_receipt_lines (receipt_id);

CREATE INDEX idx_stock_receipt_lines_product_id
    ON stock_receipt_lines (product_id);
