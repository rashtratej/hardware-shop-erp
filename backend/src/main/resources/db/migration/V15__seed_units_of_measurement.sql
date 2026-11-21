
-- V15: Seed standard units of measurement

INSERT INTO uoms (code, display_name, quantity_scale)
VALUES
    ('PCS', 'Pieces', 0),
    ('DOZ', 'Dozen', 0),
    ('PAIR', 'Pair', 0),
    ('SET', 'Set', 0),
    ('BOX', 'Box', 0),
    ('PACK', 'Pack', 0),
    ('BAG', 'Bag', 0),
    ('ROLL', 'Roll', 0),
    ('BUNDLE', 'Bundle', 0),

    ('KG', 'Kilogram', 3),
    ('G', 'Gram', 3),
    ('TON', 'Metric Tonne', 3),

    ('MTR', 'Metre', 3),
    ('CM', 'Centimetre', 2),
    ('MM', 'Millimetre', 2),
    ('INCH', 'Inch', 3),
    ('FT', 'Foot', 3),

    ('SQFT', 'Square Foot', 3),
    ('SQM', 'Square Metre', 3),

    ('CFT', 'Cubic Foot', 3),
    ('CBM', 'Cubic Metre', 3),

    ('LTR', 'Litre', 3),
    ('ML', 'Millilitre', 3),
    ('GAL', 'Gallon', 3);
