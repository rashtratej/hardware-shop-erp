
-- V10: Create financial-year invoice sequences.

CREATE TABLE invoice_sequences (
                                   financial_year CHAR(9) PRIMARY KEY,

                                   next_number BIGINT NOT NULL
                                       CHECK (next_number > 0),

                                   updated_at TIMESTAMPTZ NOT NULL
                                       DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT chk_invoice_sequences_financial_year
                                       CHECK (
                                           financial_year ~ '^[0-9]{4}-[0-9]{4}$'
            AND substring(financial_year, 6, 4)::INTEGER =
                substring(financial_year, 1, 4)::INTEGER + 1
                                           )
);