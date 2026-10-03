-- Fatia 33: histórico imutável de publicação. tipo_solicitacao (V004) e o
-- snapshot em solicitacao não são recriados. Solicitação em voo não é migrada.

CREATE TABLE tipo_solicitacao_versao (
    id              UUID            PRIMARY KEY,
    tipo_id         UUID            NOT NULL REFERENCES tipo_solicitacao (id),
    versao          INTEGER         NOT NULL,
    form_schema     JSONB           NOT NULL,
    workflow_json   JSONB           NOT NULL,
    publicado_em    TIMESTAMPTZ     NOT NULL,
    publicado_por   UUID,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    UNIQUE (tipo_id, versao)
);

CREATE INDEX idx_tipo_solicitacao_versao_tipo ON tipo_solicitacao_versao (tipo_id, versao DESC);
