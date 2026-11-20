
-- V11: Create invoices and invoice lines.

CREATE TABLE invoices (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          invoice_number VARCHAR(40) NOT NULL,
                          financial_year CHAR(9) NOT NULL,

                          document_status VARCHAR(12) NOT NULL
                              CHECK (document_status IN ('ISSUED', 'VOID')),

                          invoice_date TIMESTAMPTZ NOT NULL,

                          customer_id UUID NOT NULL
                              REFERENCES customers(id) ON DELETE RESTRICT,

                          default_warehouse_id UUID
                              REFERENCES warehouses(id) ON DELETE RESTRICT,

    -- Seller identity snapshots
                          seller_name_snapshot VARCHAR(160) NOT NULL,
                          seller_gstin_snapshot CHAR(15),
                          seller_address_snapshot TEXT NOT NULL,
                          seller_state_code_snapshot CHAR(2) NOT NULL,

    -- Customer identity snapshots
                          customer_name_snapshot VARCHAR(160) NOT NULL,
                          customer_gstin_snapshot CHAR(15),
                          customer_address_snapshot TEXT,
                          place_of_supply_state_code CHAR(2) NOT NULL,

    -- Invoice totals
                          subtotal NUMERIC(14,2) NOT NULL CHECK (subtotal >= 0),
                          discount_total NUMERIC(14,2) NOT NULL CHECK (discount_total >= 0),
                          taxable_total NUMERIC(14,2) NOT NULL CHECK (taxable_total >= 0),

                          cgst_total NUMERIC(14,2) NOT NULL DEFAULT 0
                              CHECK (cgst_total >= 0),

                          sgst_total NUMERIC(14,2) NOT NULL DEFAULT 0
                              CHECK (sgst_total >= 0),

                          igst_total NUMERIC(14,2) NOT NULL DEFAULT 0
                              CHECK (igst_total >= 0),

                          grand_total NUMERIC(14,2) NOT NULL CHECK (grand_total >= 0),

    -- Issuance and audit metadata
                          created_by UUID NOT NULL
                              REFERENCES app_users(id) ON DELETE RESTRICT,

                          issued_at TIMESTAMPTZ NOT NULL,

                          voided_at TIMESTAMPTZ,

                          voided_by UUID
                              REFERENCES app_users(id) ON DELETE RESTRICT,

                          void_reason TEXT,

                          CONSTRAINT uq_invoices_financial_year_number
                              UNIQUE (financial_year, invoice_number),

                          CONSTRAINT chk_invoices_financial_year
                              CHECK (
                                  financial_year ~ '^[0-9]{4}-[0-9]{4}$'
            AND substring(financial_year, 6, 4)::INTEGER =
                substring(financial_year, 1, 4)::INTEGER + 1
                                  ),

                          CONSTRAINT chk_invoices_seller_state_code
                              CHECK (seller_state_code_snapshot ~ '^[0-9]{2}$'),

                          CONSTRAINT chk_invoices_place_of_supply_state_code
                              CHECK (place_of_supply_state_code ~ '^[0-9]{2}$'),

                          CONSTRAINT chk_invoices_void_metadata
                              CHECK (
                                  (
                                      document_status = 'ISSUED'
                                          AND voided_at IS NULL
                                          AND voided_by IS NULL
                                          AND void_reason IS NULL
                                      )
                                      OR
                                  (
                                      document_status = 'VOID'
                                          AND voided_at IS NOT NULL
                                          AND voided_by IS NOT NULL
                                          AND void_reason IS NOT NULL
                                          AND btrim(void_reason) <> ''
                                      )
                                  ),

                          CONSTRAINT chk_invoices_seller_gstin_format
                              CHECK (
                                  seller_gstin_snapshot IS NULL
                                      OR seller_gstin_snapshot::text ~
                '^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$'
                                  ),

                          CONSTRAINT chk_invoices_customer_gstin_format
                              CHECK (
                                  customer_gstin_snapshot IS NULL
                                      OR customer_gstin_snapshot::text ~
                '^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$'
                                  )
);

CREATE INDEX idx_invoices_customer_date
    ON invoices (customer_id, invoice_date DESC);

CREATE INDEX idx_invoices_date
    ON invoices (invoice_date DESC);

CREATE TABLE invoice_lines (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               invoice_id UUID NOT NULL
                                   REFERENCES invoices(id) ON DELETE RESTRICT,

                               line_number INTEGER NOT NULL CHECK (line_number > 0),

                               warehouse_id UUID NOT NULL
                                   REFERENCES warehouses(id) ON DELETE RESTRICT,

                               product_id UUID NOT NULL
                                   REFERENCES products(id) ON DELETE RESTRICT,

    -- Historical product snapshots
                               sku_snapshot VARCHAR(64) NOT NULL,
                               product_name_snapshot VARCHAR(180) NOT NULL,
                               hsn_code_snapshot VARCHAR(12) NOT NULL,
                               uom_code_snapshot VARCHAR(16) NOT NULL,

    -- Quantity and pricing
                               quantity NUMERIC(14,3) NOT NULL CHECK (quantity > 0),

                               unit_sale_price NUMERIC(14,2) NOT NULL
                                   CHECK (unit_sale_price >= 0),

                               discount_amount NUMERIC(14,2) NOT NULL DEFAULT 0
                                   CHECK (discount_amount >= 0),

                               taxable_value NUMERIC(14,2) NOT NULL
                                   CHECK (taxable_value >= 0),

    -- GST snapshots
                               gst_rate_snapshot NUMERIC(5,2) NOT NULL
                                   CHECK (gst_rate_snapshot >= 0),

                               cgst_amount NUMERIC(14,2) NOT NULL DEFAULT 0
                                   CHECK (cgst_amount >= 0),

                               sgst_amount NUMERIC(14,2) NOT NULL DEFAULT 0
                                   CHECK (sgst_amount >= 0),

                               igst_amount NUMERIC(14,2) NOT NULL DEFAULT 0
                                   CHECK (igst_amount >= 0),

                               line_total NUMERIC(14,2) NOT NULL
                                   CHECK (line_total >= 0),

    -- Internal cost snapshot; Admin-only API exposure
                               unit_cost_snapshot NUMERIC(14,4) NOT NULL
                                   CHECK (unit_cost_snapshot >= 0),

                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT uq_invoice_lines_invoice_line
                                   UNIQUE (invoice_id, line_number)
);

CREATE INDEX idx_invoice_lines_product_id
    ON invoice_lines (product_id);

CREATE INDEX idx_invoice_lines_warehouse_id
    ON invoice_lines (warehouse_id);
