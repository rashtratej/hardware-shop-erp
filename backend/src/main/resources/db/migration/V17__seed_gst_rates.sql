-- V17: Seed standard GST rates for product catalogue

INSERT INTO gst_rates (
    code,
    description,
    gst_rate,
    effective_from,
    effective_to,
    active
)
VALUES
    ('GST_0', 'GST Exempt', 0.00, '2026-04-01', NULL, TRUE),
    ('GST_5', 'GST 5 Percent', 5.00, '2026-04-01', NULL, TRUE),
    ('GST_12', 'GST 12 Percent', 12.00, '2026-04-01', NULL, TRUE),
    ('GST_18', 'GST 18 Percent', 18.00, '2026-04-01', NULL, TRUE),
    ('GST_28', 'GST 28 Percent', 28.00, '2026-04-01', NULL, TRUE);