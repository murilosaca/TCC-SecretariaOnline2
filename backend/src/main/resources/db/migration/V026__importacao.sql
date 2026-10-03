-- Fatia 29 / F5.16: job de importação e a alocação que o kind alocacao_professor grava.
-- Validação assíncrona no processo; sem RabbitMQ e sem agenda recorrente.

CREATE TABLE import_job (
    id                  UUID            PRIMARY KEY,
    kind                VARCHAR(40)     NOT NULL,
    status              VARCHAR(20)     NOT NULL,
    operador_id         UUID            NOT NULL,
    nome_arquivo        VARCHAR(255)    NOT NULL,
    checksum_sha256     VARCHAR(64)     NOT NULL,
    total_linhas        INTEGER         NOT NULL,
    valid_count         INTEGER         NOT NULL DEFAULT 0,
    error_count         INTEGER         NOT NULL DEFAULT 0,
    warning_count       INTEGER         NOT NULL DEFAULT 0,
    importadas          INTEGER         NOT NULL DEFAULT 0,
    mensagem            TEXT,
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_import_job_operador ON import_job (operador_id, created_at DESC);

CREATE TABLE import_linha (
    id                  UUID            PRIMARY KEY,
    import_job_id       UUID            NOT NULL REFERENCES import_job (id),
    numero              INTEGER         NOT NULL,
    status              VARCHAR(20)     NOT NULL,
    mensagem            TEXT,
    conteudo            TEXT            NOT NULL,
    CONSTRAINT uk_import_linha_job_numero UNIQUE (import_job_id, numero)
);

CREATE INDEX idx_import_linha_job ON import_linha (import_job_id, numero);

CREATE TABLE alocacao_professor (
    id                  UUID            PRIMARY KEY,
    id_usuario          UUID            NOT NULL REFERENCES usuario (id),
    id_disciplina       UUID            NOT NULL REFERENCES disciplina (id),
    id_curso            UUID            NOT NULL REFERENCES curso (id),
    created_at          TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uk_alocacao_professor_disciplina UNIQUE (id_usuario, id_disciplina)
);

CREATE INDEX idx_alocacao_professor_curso ON alocacao_professor (id_curso);
