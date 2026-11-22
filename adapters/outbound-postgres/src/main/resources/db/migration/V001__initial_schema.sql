-- =============================================================================
-- V001 — Initial schema for Taxi Rank Manager
-- =============================================================================

-- -------------------------------------------------------------------------
-- Identity
-- -------------------------------------------------------------------------
CREATE TABLE app_user (
    id              UUID        PRIMARY KEY,
    external_sub    VARCHAR(255) NOT NULL UNIQUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE app_user_role (
    user_id         UUID        NOT NULL REFERENCES app_user(id),
    role            VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- -------------------------------------------------------------------------
-- Rank
-- -------------------------------------------------------------------------
CREATE TABLE rank (
    id                   UUID         PRIMARY KEY,
    name                 VARCHAR(200) NOT NULL,
    location_description TEXT,
    active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE rank_administrator (
    rank_id      UUID NOT NULL REFERENCES rank(id),
    user_id      UUID NOT NULL REFERENCES app_user(id),
    assigned_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (rank_id, user_id)
);

-- -------------------------------------------------------------------------
-- Route
-- -------------------------------------------------------------------------
CREATE TABLE route (
    id          UUID         PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    origin      VARCHAR(200) NOT NULL,
    destination VARCHAR(200) NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE rank_route (
    id         UUID    PRIMARY KEY,
    rank_id    UUID    NOT NULL REFERENCES rank(id),
    route_id   UUID    NOT NULL REFERENCES route(id),
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (rank_id, route_id)
);

CREATE TABLE marshal_rank_route (
    rank_route_id UUID NOT NULL REFERENCES rank_route(id),
    user_id       UUID NOT NULL REFERENCES app_user(id),
    assigned_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (rank_route_id, user_id)
);

-- -------------------------------------------------------------------------
-- Fleet
-- -------------------------------------------------------------------------
CREATE TABLE driver (
    id             UUID         PRIMARY KEY,
    id_number_hash VARCHAR(64)  NOT NULL UNIQUE,  -- hashed for lookup; raw value encrypted at rest
    id_number_enc  BYTEA,                          -- AES-encrypted ID number
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    mobile_number  VARCHAR(20),
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    version        BIGINT       NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE taxi (
    id                   UUID         PRIMARY KEY,
    registration_display VARCHAR(20)  NOT NULL,
    registration_norm    VARCHAR(20)  NOT NULL UNIQUE,
    active               BOOLEAN      NOT NULL DEFAULT TRUE,
    version              BIGINT       NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE driver_taxi_assignment (
    driver_id   UUID        NOT NULL REFERENCES driver(id),
    taxi_id     UUID        NOT NULL REFERENCES taxi(id),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (driver_id, taxi_id)
);

CREATE TABLE taxi_rank_route_assignment (
    taxi_id       UUID        NOT NULL REFERENCES taxi(id),
    rank_route_id UUID        NOT NULL REFERENCES rank_route(id),
    assigned_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active        BOOLEAN     NOT NULL DEFAULT TRUE,
    -- One taxi has at most one active route at a given rank
    PRIMARY KEY (taxi_id, rank_route_id)
);

-- -------------------------------------------------------------------------
-- Queue
-- -------------------------------------------------------------------------
CREATE TABLE daily_queue (
    id               UUID        PRIMARY KEY,
    rank_route_id    UUID        NOT NULL REFERENCES rank_route(id),
    service_date     DATE        NOT NULL,
    lifecycle_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version          BIGINT      NOT NULL DEFAULT 0,
    created_by       UUID        REFERENCES app_user(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_daily_queue_rank_route_date
    ON daily_queue (rank_route_id, service_date)
    WHERE lifecycle_status NOT IN ('CANCELLED');

CREATE TABLE queue_entry (
    id         UUID        PRIMARY KEY,
    queue_id   UUID        NOT NULL REFERENCES daily_queue(id),
    taxi_id    UUID        NOT NULL REFERENCES taxi(id),
    position   INT         NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    version    BIGINT      NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (queue_id, taxi_id),
    UNIQUE (queue_id, position)
);

CREATE TABLE queue_event (
    id                UUID        PRIMARY KEY,
    queue_id          UUID        NOT NULL REFERENCES daily_queue(id),
    entry_id          UUID        REFERENCES queue_entry(id),
    event_type        VARCHAR(50) NOT NULL,
    previous_status   VARCHAR(20),
    new_status        VARCHAR(20),
    previous_position INT,
    new_position      INT,
    reason            TEXT,
    actor_user_id     UUID        REFERENCES app_user(id),
    occurred_at       TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_queue_event_queue_id ON queue_event (queue_id);

-- -------------------------------------------------------------------------
-- Notification outbox
-- -------------------------------------------------------------------------
CREATE TABLE notification_outbox (
    id                 UUID        PRIMARY KEY,
    driver_id          UUID        NOT NULL REFERENCES driver(id),
    mobile_number      VARCHAR(20) NOT NULL,
    event_type         VARCHAR(50) NOT NULL,
    message_text       TEXT        NOT NULL,
    delivery_status    VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    provider_reference VARCHAR(100),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at       TIMESTAMPTZ
);

CREATE INDEX idx_notification_outbox_pending
    ON notification_outbox (created_at)
    WHERE delivery_status = 'PENDING';
