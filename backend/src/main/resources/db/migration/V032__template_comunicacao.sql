-- Fatia 34: cabeçalho do template e revisão imutável. pg_trgm permanece na V001.

CREATE TABLE template_comunicacao (
    id          UUID            PRIMARY KEY,
    nome        VARCHAR(120)    NOT NULL UNIQUE,
    assunto     VARCHAR(200)    NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL,
    updated_at  TIMESTAMPTZ     NOT NULL
);

CREATE TABLE template_comunicacao_revisao (
    id           UUID            PRIMARY KEY,
    template_id  UUID            NOT NULL REFERENCES template_comunicacao (id),
    versao       INTEGER         NOT NULL,
    assunto      VARCHAR(200)    NOT NULL,
    corpo        TEXT            NOT NULL,
    status       VARCHAR(16)     NOT NULL,
    autor_id     UUID            NOT NULL,
    created_at   TIMESTAMPTZ     NOT NULL,
    updated_at   TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uk_template_revisao UNIQUE (template_id, versao),
    CONSTRAINT ck_template_revisao_status CHECK (status IN ('CURRENT', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_template_revisao_current
    ON template_comunicacao_revisao (template_id)
    WHERE status = 'CURRENT';

CREATE INDEX idx_template_revisao_template
    ON template_comunicacao_revisao (template_id, versao DESC);
