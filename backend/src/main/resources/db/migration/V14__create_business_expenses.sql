
-- V14: Create business expenses.

CREATE TABLE business_expenses (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                   expense_date DATE NOT NULL,

                                   category VARCHAR(60) NOT NULL,

                                   description VARCHAR(240) NOT NULL,

                                   amount_ex_gst NUMERIC(14,2)
                                       CHECK (amount_ex_gst >= 0),

                                   gst_amount NUMERIC(14,2) NOT NULL DEFAULT 0
                                       CHECK (gst_amount >= 0),

                                   reference VARCHAR(100),

                                   recorded_by UUID NOT NULL
                                       REFERENCES app_users(id) ON DELETE RESTRICT,

                                   voided_at TIMESTAMPTZ,

                                   voided_by UUID
                                       REFERENCES app_users(id) ON DELETE RESTRICT,

                                   void_reason TEXT,

                                   created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT chk_business_expenses_void_metadata
                                       CHECK (
                                           (
                                               voided_at IS NULL
                                                   AND voided_by IS NULL
                                                   AND void_reason IS NULL
                                               )
                                               OR
                                           (
                                               voided_at IS NOT NULL
                                                   AND voided_by IS NOT NULL
                                                   AND void_reason IS NOT NULL
                                                   AND btrim(void_reason) <> ''
                                               )
                                           )
);

CREATE INDEX idx_business_expenses_date
    ON business_expenses (expense_date DESC);

CREATE INDEX idx_business_expenses_category_date
    ON business_expenses (category, expense_date DESC);
