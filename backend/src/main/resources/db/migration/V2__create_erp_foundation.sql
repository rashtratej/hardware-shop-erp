
-- V2: ERP foundation tables

-- One legal business per deployment.
CREATE TABLE shop_settings (
                               id SMALLINT PRIMARY KEY CHECK (id = 1),

                               legal_name VARCHAR(160) NOT NULL,
                               trade_name VARCHAR(160),

                               gstin CHAR(15),

                               address_line1 VARCHAR(200) NOT NULL,
                               address_line2 VARCHAR(200),
                               city VARCHAR(100) NOT NULL,
                               state_code CHAR(2) NOT NULL,
                               postal_code VARCHAR(10) NOT NULL,

                               currency_code CHAR(3) NOT NULL DEFAULT 'INR',
                               timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata',
                               invoice_prefix VARCHAR(20) NOT NULL,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT chk_shop_settings_gstin
                                   CHECK (
                                       gstin IS NULL OR
                                       btrim(gstin) ~ '^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$'
                                       )
);

-- Application users and backend roles.
CREATE TABLE app_users (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           email CITEXT NOT NULL UNIQUE,
                           display_name VARCHAR(120) NOT NULL,
                           password_hash VARCHAR(255) NOT NULL,

                           role VARCHAR(16) NOT NULL
                               CHECK (role IN ('ADMIN', 'STAFF')),

                           active BOOLEAN NOT NULL DEFAULT TRUE,
                           last_login_at TIMESTAMPTZ,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Physical stock locations.
CREATE TABLE warehouses (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                            code VARCHAR(30) NOT NULL UNIQUE,
                            name VARCHAR(120) NOT NULL,
                            address TEXT,

                            active BOOLEAN NOT NULL DEFAULT TRUE,

                            created_by UUID
                                REFERENCES app_users(id) ON DELETE RESTRICT,

                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Useful lookup index for warehouse administration.
CREATE INDEX idx_warehouses_active
    ON warehouses(active);
