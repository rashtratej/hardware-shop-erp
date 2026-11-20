
-- V13: Create customer payments and invoice payment allocations.

CREATE TABLE customer_payments (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                   customer_id UUID NOT NULL
                                       REFERENCES customers(id) ON DELETE RESTRICT,

                                   payment_type VARCHAR(12) NOT NULL
                                       CHECK (payment_type IN ('RECEIPT', 'REFUND')),

                                   amount NUMERIC(14,2) NOT NULL
                                       CHECK (amount > 0),

                                   payment_method VARCHAR(20) NOT NULL
                                       CHECK (
                                           payment_method IN (
                                                              'CASH',
                                                              'UPI',
                                                              'BANK_TRANSFER',
                                                              'CARD',
                                                              'CHEQUE',
                                                              'OTHER'
                                               )
                                           ),

                                   transaction_reference VARCHAR(100),

                                   received_at TIMESTAMPTZ NOT NULL,

                                   note TEXT,

                                   recorded_by UUID NOT NULL
                                       REFERENCES app_users(id) ON DELETE RESTRICT,

                                   idempotency_key UUID NOT NULL UNIQUE,

                                   created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customer_payments_customer_received
    ON customer_payments (customer_id, received_at DESC);


CREATE TABLE payment_allocations (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                     payment_id UUID NOT NULL
                                         REFERENCES customer_payments(id) ON DELETE RESTRICT,

                                     invoice_id UUID NOT NULL
                                         REFERENCES invoices(id) ON DELETE RESTRICT,

                                     amount NUMERIC(14,2) NOT NULL
                                         CHECK (amount > 0),

                                     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT uq_payment_allocations_payment_invoice
                                         UNIQUE (payment_id, invoice_id)
);

CREATE INDEX idx_payment_allocations_invoice_id
    ON payment_allocations (invoice_id);
