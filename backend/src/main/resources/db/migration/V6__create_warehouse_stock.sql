-- V6: Create per-product, per-warehouse stock balances.

CREATE TABLE warehouse_stock (
                                 warehouse_id UUID NOT NULL
                                     REFERENCES warehouses(id) ON DELETE RESTRICT,

                                 product_id UUID NOT NULL
                                     REFERENCES products(id) ON DELETE RESTRICT,

                                 quantity_on_hand NUMERIC(14,3) NOT NULL DEFAULT 0
                                     CHECK (quantity_on_hand >= 0),

                                 weighted_avg_cost NUMERIC(14,4) NOT NULL DEFAULT 0
                                     CHECK (weighted_avg_cost >= 0),

                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 version BIGINT NOT NULL DEFAULT 0
                                     CHECK (version >= 0),

                                 CONSTRAINT pk_warehouse_stock
                                     PRIMARY KEY (warehouse_id, product_id)
);

CREATE INDEX idx_warehouse_stock_product_id
    ON warehouse_stock (product_id, warehouse_id);