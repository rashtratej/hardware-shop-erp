-- V23: Add missing shipping postal code field to customers.

ALTER TABLE public.customers
    ADD COLUMN shipping_postal_code VARCHAR(10);
