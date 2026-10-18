CREATE TABLE users
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email        VARCHAR(320) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_ts   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_ts   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_user_status
        CHECK ( status IN ('ACTIVE', 'DISABLED') )
);

CREATE UNIQUE INDEX ux_users_email
    ON users (LOWER(email));

CREATE TABLE user_credentials
(
    user_id       UUID PRIMARY KEY,
    password_hash TEXT        NOT NULL,
    created_ts    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_ts    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_credentials_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);
