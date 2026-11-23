-- V19: Supplier management

CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(180) NOT NULL,
    gstin VARCHAR(15),

    contact_person VARCHAR(120),
    phone VARCHAR(20),
    email VARCHAR(255),

    address_line1 VARCHAR(200),
    address_line2 VARCHAR(200),
    city VARCHAR(100),
    state VARCHAR(100),
    state_code VARCHAR(2),
    postal_code VARCHAR(10),

    notes VARCHAR(500),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_by UUID NOT NULL
        REFERENCES app_users(id) ON DELETE RESTRICT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_suppliers_gstin
        CHECK (
            gstin IS NULL OR
            gstin ~ '^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$'
        ),

    CONSTRAINT chk_suppliers_state_code
        CHECK (state_code IS NULL OR state_code ~ '^[0-9]{2}$')
);

-- A GSTIN can belong to only one supplier.
-- NULL GSTIN values are allowed for unregistered suppliers.
CREATE UNIQUE INDEX uq_suppliers_gstin
    ON suppliers(gstin)
    WHERE gstin IS NOT NULL;

CREATE INDEX idx_suppliers_name
    ON suppliers(name);

CREATE INDEX idx_suppliers_active
    ON suppliers(active);

CREATE INDEX idx_suppliers_created_at
    ON suppliers(created_at DESC);
