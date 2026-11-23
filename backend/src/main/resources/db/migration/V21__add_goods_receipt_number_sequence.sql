CREATE SEQUENCE goods_receipt_number_seq START WITH 1;

ALTER TABLE goods_receipts
    ALTER COLUMN receipt_number
    SET DEFAULT (
        'GRN-' ||
        TO_CHAR(CURRENT_DATE, 'YYYYMM') ||
        '-' ||
        LPAD(NEXTVAL('goods_receipt_number_seq')::TEXT, 6, '0')
    );
