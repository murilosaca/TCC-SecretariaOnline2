-- Certificado oficial emitido só pelo sistema (RF-TR-003 / RF-F1-010 / RF-F0-007).
-- Bytes do PDF nesta fatia: bytea. S3/MinIO fica para quando o compose tiver MinIO.

CREATE TABLE certificado (
    id                  UUID            PRIMARY KEY,
    id_aluno            UUID            NOT NULL REFERENCES aluno (id),
    id_formativa        UUID            REFERENCES formativa (id),
    id_evento           UUID            REFERENCES evento (id),
    tipo                VARCHAR(40)     NOT NULL,
    titulo              VARCHAR(200)    NOT NULL,
    carga_horaria       INTEGER         NOT NULL,
    beneficiario_nome   VARCHAR(200)    NOT NULL,
    hash_sha256         VARCHAR(64)     NOT NULL,
    assinatura          TEXT            NOT NULL,
    pdf                 BYTEA           NOT NULL,
    emitido_em          TIMESTAMPTZ     NOT NULL,
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uq_certificado_formativa UNIQUE (id_formativa),
    CONSTRAINT uq_certificado_hash UNIQUE (hash_sha256)
);

CREATE INDEX idx_certificado_aluno ON certificado (id_aluno);
CREATE INDEX idx_certificado_aluno_emitido ON certificado (id_aluno, emitido_em DESC);
