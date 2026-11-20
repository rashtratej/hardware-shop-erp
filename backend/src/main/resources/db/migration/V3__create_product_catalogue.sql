
-- V3: Units of measurement, GST rates and products

CREATE TABLE uoms (
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                      code VARCHAR(16) NOT NULL UNIQUE,
                      display_name VARCHAR(40) NOT NULL,

                      quantity_scale SMALLINT NOT NULL
                          CHECK (quantity_scale BETWEEN 0 AND 3),

                      active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE gst_rates (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           code VARCHAR(32) NOT NULL,
                           description VARCHAR(100) NOT NULL,

                           gst_rate NUMERIC(5,2) NOT NULL
                               CHECK (gst_rate BETWEEN 0 AND 100),

                           effective_from DATE NOT NULL,
                           effective_to DATE,

                           active BOOLEAN NOT NULL DEFAULT TRUE,

                           created_by UUID
                               REFERENCES app_users(id) ON DELETE RESTRICT,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT chk_gst_rates_effective_dates
                               CHECK (
                                   effective_to IS NULL OR effective_to >= effective_from
                                   ),

                           CONSTRAINT uq_gst_rates_code_effective_from
                               UNIQUE (code, effective_from)
);

CREATE TABLE products (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          sku CITEXT NOT NULL UNIQUE,
                          name VARCHAR(180) NOT NULL,
                          description TEXT,

                          hsn_code VARCHAR(12) NOT NULL,

                          uom_id UUID NOT NULL
                              REFERENCES uoms(id) ON DELETE RESTRICT,

                          gst_rate_id UUID NOT NULL
                              REFERENCES gst_rates(id) ON DELETE RESTRICT,

                          reorder_level NUMERIC(14,3) NOT NULL DEFAULT 0
                              CHECK (reorder_level >= 0),

                          default_sale_price NUMERIC(14,2) NOT NULL
                              CHECK (default_sale_price >= 0),

                          active BOOLEAN NOT NULL DEFAULT TRUE,

                          created_by UUID
                              REFERENCES app_users(id) ON DELETE RESTRICT,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Product name lookup.
-- SKU already has an index from its UNIQUE constraint.
CREATE INDEX idx_products_name
    ON products(name);

-- Useful for filtering products by their UOM or GST configuration.
CREATE INDEX idx_products_uom_id
    ON products(uom_id);

CREATE INDEX idx_products_gst_rate_id
    ON products(gst_rate_id);
