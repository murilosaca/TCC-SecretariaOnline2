CREATE TABLE usuario (
    id                          UUID            PRIMARY KEY,
    email_institucional         CITEXT          NOT NULL UNIQUE,
    email_pessoal               CITEXT          UNIQUE,
    grr                         VARCHAR(11)     UNIQUE,
    senha_hash                  VARCHAR(255)    NOT NULL,
    senha_alterada              BOOLEAN         NOT NULL DEFAULT FALSE,
    lgpd_aceite_em              TIMESTAMPTZ,
    lgpd_aceite_ip              VARCHAR(64),
    lgpd_aceite_user_agent      VARCHAR(300),
    ativo                       BOOLEAN         NOT NULL DEFAULT TRUE,
    falhas_consecutivas         INTEGER         NOT NULL DEFAULT 0,
    bloqueado_ate               TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ     NOT NULL,
    updated_at                  TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_usuario_grr ON usuario (grr);

CREATE TABLE usuario_authority (
    usuario_id      UUID            NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    authority       VARCHAR(80)     NOT NULL,
    PRIMARY KEY (usuario_id, authority)
);

CREATE TABLE refresh_token (
    id              UUID            PRIMARY KEY,
    usuario_id      UUID            NOT NULL REFERENCES usuario (id),
    token_hash      VARCHAR(64)     NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ     NOT NULL,
    used            BOOLEAN         NOT NULL DEFAULT FALSE,
    revoked         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_refresh_token_usuario ON refresh_token (usuario_id);

CREATE TABLE senha_historico (
    id              UUID            PRIMARY KEY,
    usuario_id      UUID            NOT NULL REFERENCES usuario (id),
    senha_hash      VARCHAR(255)    NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_senha_historico_usuario ON senha_historico (usuario_id, created_at DESC);

CREATE TABLE jti_blacklist (
    jti             VARCHAR(64)     PRIMARY KEY,
    expires_at      TIMESTAMPTZ     NOT NULL,
    consumed_at     TIMESTAMPTZ     NOT NULL
);

CREATE TABLE outbox_event (
    id              UUID            PRIMARY KEY,
    tipo            VARCHAR(80)     NOT NULL,
    payload         TEXT            NOT NULL,
    status          VARCHAR(20)     NOT NULL,
    tentativas      INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_outbox_event_status ON outbox_event (status, created_at);

CREATE TABLE audit_log (
    id              UUID            PRIMARY KEY,
    tipo            VARCHAR(80)     NOT NULL,
    ator_id         UUID,
    payload         TEXT            NOT NULL,
    ip              VARCHAR(64),
    created_at      TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_audit_log_tipo ON audit_log (tipo, created_at DESC);

CREATE RULE audit_log_no_update AS ON UPDATE TO audit_log DO INSTEAD NOTHING;
CREATE RULE audit_log_no_delete AS ON DELETE TO audit_log DO INSTEAD NOTHING;
