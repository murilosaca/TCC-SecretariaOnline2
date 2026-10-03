-- Fatia 29 / F5.17: job de exportação assíncrona. Download por URL pré-assinada.
-- Expiração é calculada na leitura (expires_at). Sem scheduler recorrente.

CREATE TABLE export_job (
    id                  UUID            PRIMARY KEY,
    kind                VARCHAR(40)     NOT NULL,
    status              VARCHAR(20)     NOT NULL,
    operador_id         UUID            NOT NULL,
    filtros             TEXT,
    storage_key         VARCHAR(512),
    nome_arquivo        VARCHAR(255),
    expires_at          TIMESTAMPTZ,
    mensagem            TEXT,
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_export_job_operador ON export_job (operador_id, created_at DESC);
