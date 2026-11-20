-- V9: Create customer records.

CREATE TABLE customers (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           customer_code VARCHAR(30) NOT NULL UNIQUE,

                           customer_type VARCHAR(16) NOT NULL
                               CHECK (customer_type IN ('RETAIL', 'CONTRACTOR')),

                           display_name VARCHAR(160) NOT NULL,

                           phone VARCHAR(20),

                           email CITEXT,

                           gstin CHAR(15) UNIQUE,

                           billing_address TEXT,

                           state_code CHAR(2),

                           credit_limit NUMERIC(14,2) NOT NULL DEFAULT 0
                               CHECK (credit_limit >= 0),

                           is_walk_in BOOLEAN NOT NULL DEFAULT FALSE,

                           active BOOLEAN NOT NULL DEFAULT TRUE,

                           created_by UUID
                               REFERENCES app_users(id) ON DELETE RESTRICT,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Only one Walk-in Customer is allowed.
CREATE UNIQUE INDEX uq_customers_single_walk_in
    ON customers (is_walk_in)
    WHERE is_walk_in = TRUE;

-- Customer lookup indexes.
CREATE INDEX idx_customers_phone
    ON customers (phone);

CREATE INDEX idx_customers_display_name
    ON customers (lower(display_name));

-- Validate GSTIN format when supplied.
ALTER TABLE customers
    ADD CONSTRAINT chk_customers_gstin_format
        CHECK (
            gstin IS NULL
                OR gstin::text ~
        '^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$'
            );

-- Validate Indian state code format when supplied.
ALTER TABLE customers
    ADD CONSTRAINT chk_customers_state_code
        CHECK (
            state_code IS NULL
                OR state_code ~ '^[0-9]{2}$'
            );