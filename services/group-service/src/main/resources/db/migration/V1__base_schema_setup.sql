CREATE TABLE groups
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name       VARCHAR(150) NOT NULL,
    created_by UUID         NOT NULL, -- Reference to User Service, not a foreign key
    created_ts TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    UNIQUE (name, created_by)
);

CREATE TABLE group_memberships
(
    group_id  UUID        NOT NULL REFERENCES groups (id),
    user_id   UUID        NOT NULL, -- Reference to User Service, not a foreign key
    role      VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_ts TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (group_id, user_id),
    CONSTRAINT chk_user_grouprole
        CHECK ( role in ('OWNER', 'MEMBER') )
);

-- Index to speed up the lookup of the groups the user is in.
CREATE INDEX idx_group_memberships_user_id
    ON group_memberships (user_id);

CREATE TABLE group_invitations
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    group_id     UUID        NOT NULL REFERENCES groups (id),
    invited_user UUID        NOT NULL, -- Reference to User Service, not a foreign key
    invited_by   UUID        NOT NULL, -- Reference to User Service, not a foreign key
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    responded_ts TIMESTAMPTZ,
    created_ts   TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_groupinvitation_status
        CHECK ( status in ('PENDING', 'ACCEPTED', 'DECLINED', 'REVOKED') )
);

CREATE UNIQUE INDEX uq_pending_groupinvitation
    ON group_invitations (group_id, invited_user)
    WHERE status = 'PENDING';