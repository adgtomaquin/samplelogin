-- =============================================================================
--  Vault Auth API — DDL  (H2 compatible, used for tests only)
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    id                    UUID          NOT NULL DEFAULT RANDOM_UUID(),
    email                 VARCHAR(255)  NOT NULL,
    password_hash         VARCHAR(255)  NOT NULL DEFAULT '',
    name                  VARCHAR(255),
    avatar                VARCHAR(10),
    department            VARCHAR(100),
    status                VARCHAR(20)   NOT NULL DEFAULT 'active',
    enabled               BOOLEAN       NOT NULL DEFAULT TRUE,
    failed_login_attempts INT           NOT NULL DEFAULT 0,
    locked_until          TIMESTAMP WITH TIME ZONE,
    last_login            TIMESTAMP WITH TIME ZONE,
    login_count           INT           NOT NULL DEFAULT 0,
    joined                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_users       PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_attempts CHECK (failed_login_attempts >= 0)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID        NOT NULL,
    role    VARCHAR(50) NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_ur_user    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         UUID         NOT NULL DEFAULT RANDOM_UUID(),
    token      VARCHAR(512) NOT NULL,
    user_id    UUID         NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token  UNIQUE (token),
    CONSTRAINT fk_rt_user        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS auth_audit_log (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    UUID,
    event      VARCHAR(50)  NOT NULL,
    severity   VARCHAR(20)  NOT NULL DEFAULT 'info',
    actor      VARCHAR(255),
    target     VARCHAR(255),
    ip_address VARCHAR(45),
    location   VARCHAR(255),
    user_agent TEXT,
    device     TEXT,
    request_id VARCHAR(64),
    detail     TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_auth_audit PRIMARY KEY (id),
    CONSTRAINT fk_aal_user   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS notifications (
    id         UUID          NOT NULL DEFAULT RANDOM_UUID(),
    type       VARCHAR(20)   NOT NULL,
    title      VARCHAR(255)  NOT NULL,
    message    TEXT          NOT NULL,
    read       BOOLEAN       NOT NULL DEFAULT FALSE,
    user_id    UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notif_user    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_preferences (
    id                       UUID     NOT NULL DEFAULT RANDOM_UUID(),
    user_id                  UUID     NOT NULL UNIQUE,
    timezone                 VARCHAR(50)  NOT NULL DEFAULT 'Asia/Manila',
    language                 VARCHAR(10)  NOT NULL DEFAULT 'en',
    email_on_new_login       BOOLEAN  NOT NULL DEFAULT TRUE,
    email_on_password_change BOOLEAN  NOT NULL DEFAULT TRUE,
    email_on_token_expiry    BOOLEAN  NOT NULL DEFAULT FALSE,
    compact_mode             BOOLEAN  NOT NULL DEFAULT FALSE,
    theme                    VARCHAR(20)  NOT NULL DEFAULT 'dark',
    CONSTRAINT pk_user_prefs PRIMARY KEY (id),
    CONSTRAINT fk_prefs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS system_settings (
    id                        BIGINT  NOT NULL DEFAULT 1,
    lockout_threshold         INT     NOT NULL DEFAULT 5,
    lockout_duration_minutes  INT     NOT NULL DEFAULT 15,
    access_token_ttl_seconds  INT     NOT NULL DEFAULT 900,
    refresh_token_ttl_days    INT     NOT NULL DEFAULT 7,
    remember_me_ttl_days      INT     NOT NULL DEFAULT 30,
    require_mfa               BOOLEAN NOT NULL DEFAULT FALSE,
    allowed_domains           VARCHAR(500) NOT NULL DEFAULT 'example.com, corp.com',
    session_concurrency_limit INT     NOT NULL DEFAULT 5,
    audit_retention_days      INT     NOT NULL DEFAULT 90,
    CONSTRAINT pk_system_settings PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS rate_limit_buckets (
    bucket_key    VARCHAR(256) NOT NULL,
    endpoint      VARCHAR(50)  NOT NULL,
    request_count INT          NOT NULL DEFAULT 0,
    window_start  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_rate_limit PRIMARY KEY (bucket_key, endpoint)
);
