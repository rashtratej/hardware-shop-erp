
-- V4: Enforce normalized, case-insensitive SKU uniqueness.

-- Reject empty SKUs and SKUs containing leading/trailing whitespace.
ALTER TABLE products
    ADD CONSTRAINT chk_products_sku_normalized
        CHECK (
            btrim(sku::text) <> ''
                AND sku::text = btrim(sku::text)
            );

-- Enforce uniqueness after trimming and case normalization.
CREATE UNIQUE INDEX uq_products_sku_normalized
    ON products (lower(btrim(sku::text)));