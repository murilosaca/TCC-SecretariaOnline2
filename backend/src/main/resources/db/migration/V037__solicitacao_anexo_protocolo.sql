ALTER TABLE solicitacao
    ADD COLUMN protocolo_storage_key VARCHAR(500);

CREATE TABLE solicitacao_anexo (
    id              UUID            PRIMARY KEY,
    solicitacao_id  UUID            NOT NULL REFERENCES solicitacao (id),
    nome            VARCHAR(255)    NOT NULL,
    content_type    VARCHAR(100)    NOT NULL,
    tamanho         INTEGER         NOT NULL,
    sha256          VARCHAR(64)     NOT NULL,
    storage_key     VARCHAR(500)    NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_solicitacao_anexo_solicitacao ON solicitacao_anexo (solicitacao_id);
