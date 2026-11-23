-- V18: Single-location inventory balances and immutable stock movement ledger

CREATE TABLE inventory_balances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    product_id UUID NOT NULL UNIQUE
        REFERENCES products(id) ON DELETE RESTRICT,

    quantity_on_hand NUMERIC(14,3) NOT NULL DEFAULT 0
        CHECK (quantity_on_hand >= 0),

    quantity_reserved NUMERIC(14,3) NOT NULL DEFAULT 0
        CHECK (quantity_reserved >= 0),

    available_quantity NUMERIC(14,3)
        GENERATED ALWAYS AS (quantity_on_hand - quantity_reserved) STORED,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_inventory_reserved_not_exceed_on_hand
        CHECK (quantity_reserved <= quantity_on_hand)
);

CREATE TABLE stock_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    product_id UUID NOT NULL
        REFERENCES products(id) ON DELETE RESTRICT,

    movement_type VARCHAR(20) NOT NULL
        CHECK (movement_type IN ('IN', 'OUT', 'ADJUSTMENT')),

    quantity_change NUMERIC(14,3) NOT NULL
        CHECK (quantity_change <> 0),

    reference_type VARCHAR(40),
    reference_id UUID,

    notes VARCHAR(500),

    created_by UUID NOT NULL
        REFERENCES app_users(id) ON DELETE RESTRICT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_stock_movement_direction
        CHECK (
            (movement_type = 'IN' AND quantity_change > 0)
            OR (movement_type = 'OUT' AND quantity_change < 0)
            OR (movement_type = 'ADJUSTMENT')
        )
);

CREATE INDEX idx_stock_movements_product_created_at
    ON stock_movements(product_id, created_at DESC);

CREATE INDEX idx_stock_movements_reference
    ON stock_movements(reference_type, reference_id);
