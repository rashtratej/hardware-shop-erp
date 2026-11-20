-- V7: Create stock adjustment headers and lines.

CREATE TABLE stock_adjustments (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                   adjustment_number VARCHAR(40) NOT NULL UNIQUE,

                                   warehouse_id UUID NOT NULL
                                       REFERENCES warehouses(id) ON DELETE RESTRICT,

                                   reason_code VARCHAR(32) NOT NULL
                                       CHECK (
                                           reason_code IN (
                                                           'DAMAGE',
                                                           'COUNT_CORRECTION',
                                                           'LOSS',
                                                           'FOUND'
                                               )
                                           ),

                                   note TEXT NOT NULL,

                                   status VARCHAR(16) NOT NULL
                                       CHECK (status IN ('DRAFT', 'POSTED', 'VOID')),

                                   created_by UUID NOT NULL
                                       REFERENCES app_users(id) ON DELETE RESTRICT,

                                   posted_by UUID
                                       REFERENCES app_users(id) ON DELETE RESTRICT,

                                   posted_at TIMESTAMPTZ,

                                   created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT chk_stock_adjustments_posted_metadata
                                       CHECK (
                                           status <> 'POSTED'
                                               OR (posted_by IS NOT NULL AND posted_at IS NOT NULL)
                                           )
);

CREATE INDEX idx_stock_adjustments_warehouse_created
    ON stock_adjustments (warehouse_id, created_at DESC);


CREATE TABLE stock_adjustment_lines (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                        adjustment_id UUID NOT NULL
                                            REFERENCES stock_adjustments(id) ON DELETE RESTRICT,

                                        product_id UUID NOT NULL
                                            REFERENCES products(id) ON DELETE RESTRICT,

                                        quantity_delta NUMERIC(14,3) NOT NULL
                                            CHECK (quantity_delta <> 0),

                                        unit_cost NUMERIC(14,4)
                                            CHECK (unit_cost IS NULL OR unit_cost >= 0),

                                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                        CONSTRAINT chk_stock_adjustment_lines_positive_cost
                                            CHECK (quantity_delta <= 0 OR unit_cost IS NOT NULL)
);

CREATE INDEX idx_stock_adjustment_lines_adjustment_id
    ON stock_adjustment_lines (adjustment_id);

CREATE INDEX idx_stock_adjustment_lines_product_id
    ON stock_adjustment_lines (product_id);