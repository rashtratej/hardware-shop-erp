-- V16: Create hierarchical product categories

CREATE TABLE product_categories (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                    name VARCHAR(100) NOT NULL,

                                    description VARCHAR(500),

                                    parent_category_id UUID
                                        REFERENCES product_categories(id)
                                            ON DELETE RESTRICT,

                                    active BOOLEAN NOT NULL DEFAULT TRUE,

                                    created_by UUID
                                        REFERENCES app_users(id)
                                            ON DELETE RESTRICT,

                                    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT chk_product_categories_name
                                        CHECK (BTRIM(name) <> ''),

                                    CONSTRAINT chk_product_categories_parent
                                        CHECK (
                                            parent_category_id IS NULL
                                                OR parent_category_id <> id
                                            )
);

-- Category names must be unique within the same parent.
-- Root categories are treated as sharing a common parent.

CREATE UNIQUE INDEX uq_product_categories_parent_name
    ON product_categories (
                           COALESCE(
        parent_category_id,
        '00000000-0000-0000-0000-000000000000'::UUID
    ),
                           LOWER(name)
        );

CREATE INDEX idx_product_categories_parent
    ON product_categories(parent_category_id);

CREATE INDEX idx_product_categories_active
    ON product_categories(active);

-- Link products to categories.
-- Nullable initially to preserve compatibility with existing products.

ALTER TABLE products
    ADD COLUMN category_id UUID;

ALTER TABLE products
    ADD CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
            REFERENCES product_categories(id)
            ON DELETE RESTRICT;

CREATE INDEX idx_products_category_id
    ON products(category_id);
