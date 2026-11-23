-- V20: Purchase orders and goods receipt management

-- Generates unique purchase order numbers.
CREATE SEQUENCE purchase_order_number_seq START WITH 1;

-- =====================================================
-- PURCHASE ORDERS
-- =====================================================

CREATE TABLE purchase_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    po_number VARCHAR(30) NOT NULL UNIQUE
        DEFAULT (
            'PO-' ||
            TO_CHAR(CURRENT_DATE, 'YYYYMM') ||
            '-' ||
            LPAD(NEXTVAL('purchase_order_number_seq')::TEXT, 6, '0')
        ),

    supplier_id UUID NOT NULL
        REFERENCES suppliers(id) ON DELETE RESTRICT,

    order_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expected_delivery_date DATE,

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT'
        CHECK (
            status IN (
                'DRAFT',
                'PENDING_APPROVAL',
                'APPROVED',
                'PARTIALLY_RECEIVED',
                'RECEIVED',
                'REJECTED',
                'CANCELLED'
            )
        ),

    subtotal NUMERIC(14,2) NOT NULL DEFAULT 0
        CHECK (subtotal >= 0),

    tax_total NUMERIC(14,2) NOT NULL DEFAULT 0
        CHECK (tax_total >= 0),

    grand_total NUMERIC(14,2) NOT NULL DEFAULT 0
        CHECK (grand_total >= 0),

    notes VARCHAR(1000),

    rejection_reason VARCHAR(500),
    cancellation_reason VARCHAR(500),

    created_by UUID NOT NULL
        REFERENCES app_users(id) ON DELETE RESTRICT,

    approved_by UUID
        REFERENCES app_users(id) ON DELETE RESTRICT,

    rejected_by UUID
        REFERENCES app_users(id) ON DELETE RESTRICT,

    cancelled_by UUID
        REFERENCES app_users(id) ON DELETE RESTRICT,

    approved_at TIMESTAMPTZ,
    rejected_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_purchase_order_delivery_date
        CHECK (
            expected_delivery_date IS NULL
            OR expected_delivery_date >= order_date
        )
);

CREATE INDEX idx_purchase_orders_supplier
    ON purchase_orders(supplier_id);

CREATE INDEX idx_purchase_orders_status
    ON purchase_orders(status);

CREATE INDEX idx_purchase_orders_order_date
    ON purchase_orders(order_date DESC);

CREATE INDEX idx_purchase_orders_created_at
    ON purchase_orders(created_at DESC);


-- =====================================================
-- PURCHASE ORDER ITEMS
-- =====================================================

CREATE TABLE purchase_order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    purchase_order_id UUID NOT NULL
        REFERENCES purchase_orders(id) ON DELETE RESTRICT,

    product_id UUID NOT NULL
        REFERENCES products(id) ON DELETE RESTRICT,

    -- Historical product information.
    sku_snapshot VARCHAR(255) NOT NULL,
    product_name_snapshot VARCHAR(180) NOT NULL,
    hsn_code_snapshot VARCHAR(12) NOT NULL,
    uom_code_snapshot VARCHAR(16) NOT NULL,

    ordered_quantity NUMERIC(14,3) NOT NULL
        CHECK (ordered_quantity > 0),

    received_quantity NUMERIC(14,3) NOT NULL DEFAULT 0
        CHECK (received_quantity >= 0),

    unit_cost NUMERIC(14,2) NOT NULL
        CHECK (unit_cost >= 0),

    gst_rate NUMERIC(5,2) NOT NULL
        CHECK (gst_rate >= 0 AND gst_rate <= 100),

    taxable_amount NUMERIC(14,2) NOT NULL
        CHECK (taxable_amount >= 0),

    tax_amount NUMERIC(14,2) NOT NULL
        CHECK (tax_amount >= 0),

    line_total NUMERIC(14,2) NOT NULL
        CHECK (line_total >= 0),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_purchase_item_received_quantity
        CHECK (received_quantity <= ordered_quantity),

    CONSTRAINT uq_purchase_order_product
        UNIQUE (purchase_order_id, product_id)
);

CREATE INDEX idx_purchase_order_items_product
    ON purchase_order_items(product_id);


-- =====================================================
-- GOODS RECEIPTS
-- =====================================================

CREATE TABLE goods_receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    receipt_number VARCHAR(30) NOT NULL UNIQUE,

    purchase_order_id UUID NOT NULL
        REFERENCES purchase_orders(id) ON DELETE RESTRICT,

    receipt_date DATE NOT NULL DEFAULT CURRENT_DATE,

    supplier_delivery_note VARCHAR(100),

    notes VARCHAR(1000),

    received_by UUID NOT NULL
        REFERENCES app_users(id) ON DELETE RESTRICT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_goods_receipts_purchase_order
    ON goods_receipts(purchase_order_id);

CREATE INDEX idx_goods_receipts_receipt_date
    ON goods_receipts(receipt_date DESC);


-- =====================================================
-- GOODS RECEIPT ITEMS
-- =====================================================

CREATE TABLE goods_receipt_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    goods_receipt_id UUID NOT NULL
        REFERENCES goods_receipts(id) ON DELETE RESTRICT,

    purchase_order_item_id UUID NOT NULL
        REFERENCES purchase_order_items(id) ON DELETE RESTRICT,

    product_id UUID NOT NULL
        REFERENCES products(id) ON DELETE RESTRICT,

    received_quantity NUMERIC(14,3) NOT NULL
        CHECK (received_quantity > 0),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_goods_receipt_purchase_item
        UNIQUE (goods_receipt_id, purchase_order_item_id)
);

CREATE INDEX idx_goods_receipt_items_product
    ON goods_receipt_items(product_id);
