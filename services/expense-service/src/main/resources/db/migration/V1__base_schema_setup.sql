-- Local coordination row; the group itself belongs to Group Service.
CREATE TABLE expense_groups
(
    group_id      UUID PRIMARY KEY,
    next_sequence BIGINT NOT NULL DEFAULT 1,

    CONSTRAINT chk_expense_group_next_sequence CHECK (next_sequence > 0)
);

CREATE TABLE expenses
(
    id             UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    group_id       UUID           NOT NULL REFERENCES expense_groups (group_id),
    paying_user_id UUID           NOT NULL,
    amount         NUMERIC(17, 2) NOT NULL,
    currency       VARCHAR(3)     NOT NULL,
    sequence       BIGINT         NOT NULL,
    created_ts     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_expense_amount CHECK (amount > 0),
    CONSTRAINT chk_expense_sequence CHECK (sequence > 0),
    CONSTRAINT uq_expense_group_sequence UNIQUE (group_id, sequence),
    CONSTRAINT chk_expense_group_currency CHECK (currency ~ '^[A-Z]{3}$')
);

CREATE TABLE expense_allocations
(
    expense_id UUID           NOT NULL REFERENCES expenses (id),
    user_id    UUID           NOT NULL,
    amount     NUMERIC(17, 2) NOT NULL,
    created_ts TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    PRIMARY KEY (expense_id, user_id),
    CONSTRAINT chk_expense_allocation_amount CHECK (amount >= 0)
);

CREATE TABLE repayments
(
    id                UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    group_id          UUID           NOT NULL REFERENCES expense_groups (group_id),
    reporting_user_id UUID           NOT NULL,
    repaying_user_id  UUID           NOT NULL,
    recipient_user_id UUID           NOT NULL,
    amount            NUMERIC(17, 2) NOT NULL,
    currency          VARCHAR(3)     NOT NULL,
    sequence          BIGINT         NOT NULL,
    created_ts        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_repayment_amount CHECK (amount > 0),
    CONSTRAINT chk_repayment_sequence CHECK (sequence > 0),
    CONSTRAINT chk_repayment_distinct_users
        CHECK (repaying_user_id <> recipient_user_id),
    CONSTRAINT chk_repayment_reporter
        CHECK (reporting_user_id IN (repaying_user_id, recipient_user_id)),
    CONSTRAINT uq_repayment_group_sequence UNIQUE (group_id, sequence),
    CONSTRAINT chk_repayments_currency CHECK (currency ~ '^[A-Z]{3}$')
);

CREATE INDEX idx_expense_allocations_user_id
    ON expense_allocations (user_id);

CREATE INDEX idx_repayments_group_repayer
    ON repayments (group_id, repaying_user_id);

CREATE INDEX idx_repayments_group_recipient
    ON repayments (group_id, recipient_user_id);
