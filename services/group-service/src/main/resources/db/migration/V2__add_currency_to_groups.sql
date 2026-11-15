ALTER TABLE groups
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD';

ALTER TABLE groups
    ADD CONSTRAINT chk_group_currency CHECK (currency ~ '^[A-Z]{3}$');