-- V22: Evolve legacy customers table for customer management.
-- Preserve existing customer IDs and invoice/payment foreign keys.

-- Add new customer fields.
ALTER TABLE public.customers
    ADD COLUMN name VARCHAR(180),
    ADD COLUMN contact_person VARCHAR(120),
    ADD COLUMN billing_address_line1 VARCHAR(200),
    ADD COLUMN billing_address_line2 VARCHAR(200),
    ADD COLUMN billing_city VARCHAR(100),
    ADD COLUMN billing_state VARCHAR(100),
    ADD COLUMN billing_state_code VARCHAR(2),
    ADD COLUMN billing_postal_code VARCHAR(10),
    ADD COLUMN shipping_address_line1 VARCHAR(200),
    ADD COLUMN shipping_address_line2 VARCHAR(200),
    ADD COLUMN shipping_city VARCHAR(100),
    ADD COLUMN shipping_state VARCHAR(100),
    ADD COLUMN shipping_state_code VARCHAR(2),
    ADD COLUMN notes VARCHAR(500);

-- Normalize column types to match the new JPA entity.
ALTER TABLE public.customers
    ALTER COLUMN customer_type TYPE VARCHAR(20),
    ALTER COLUMN gstin TYPE VARCHAR(15) USING TRIM(gstin),
    ALTER COLUMN email TYPE VARCHAR(255) USING email::text,
    ALTER COLUMN state_code TYPE VARCHAR(2) USING TRIM(state_code);

-- The new entity does not map these legacy columns.
-- Keep them for compatibility, but allow new API inserts.
ALTER TABLE public.customers
    ALTER COLUMN customer_code DROP NOT NULL,
    ALTER COLUMN display_name DROP NOT NULL,
    ALTER COLUMN credit_limit SET DEFAULT 0,
    ALTER COLUMN is_walk_in SET DEFAULT FALSE;

-- Replace legacy customer type values/check.
ALTER TABLE public.customers
    DROP CONSTRAINT customers_customer_type_check;

ALTER TABLE public.customers
    ADD CONSTRAINT chk_customers_type
    CHECK (customer_type IN ('INDIVIDUAL', 'BUSINESS'));

-- The new entity uses a name field.
ALTER TABLE public.customers
    ALTER COLUMN name SET NOT NULL;

-- Address state-code validation.
ALTER TABLE public.customers
    ADD CONSTRAINT chk_customers_billing_state_code
    CHECK (
        billing_state_code IS NULL OR
        billing_state_code ~ '^[0-9]{2}$'
    );

ALTER TABLE public.customers
    ADD CONSTRAINT chk_customers_shipping_state_code
    CHECK (
        shipping_state_code IS NULL OR
        shipping_state_code ~ '^[0-9]{2}$'
    );

-- Preserve the existing GSTIN unique constraint and format check.
-- Add indexes for new customer searches and filters.
CREATE INDEX idx_customers_name
    ON public.customers(name);

CREATE INDEX idx_customers_active
    ON public.customers(active);

CREATE INDEX idx_customers_type
    ON public.customers(customer_type);

CREATE INDEX idx_customers_created_at
    ON public.customers(created_at DESC);
