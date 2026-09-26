-- Fatia 16 / F5.11: colação de grau e diploma (RF-F5-005-b).
-- PDF oficial no MinIO via storage_key (sem bytea).

CREATE TABLE diploma (
    id                  UUID            PRIMARY KEY,
    id_aluno            UUID            NOT NULL REFERENCES aluno (id),
    id_curso            UUID            NOT NULL REFERENCES curso (id),
    id_periodo_letivo   UUID            REFERENCES periodo_letivo (id),
    numero              VARCHAR(40)     NOT NULL,
    situacao            VARCHAR(20)     NOT NULL,
    data_colacao        TIMESTAMPTZ     NOT NULL,
    livro               VARCHAR(40)     NOT NULL,
    folha               VARCHAR(40)     NOT NULL,
    turma               VARCHAR(80),
    storage_key         VARCHAR(512),
    metodo_entrega      VARCHAR(20),
    data_entrega        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uq_diploma_aluno UNIQUE (id_aluno),
    CONSTRAINT uq_diploma_numero UNIQUE (numero),
    CONSTRAINT ck_diploma_situacao CHECK (situacao IN ('PENDENTE', 'ENTREGUE')),
    CONSTRAINT ck_diploma_metodo CHECK (
        metodo_entrega IS NULL
        OR metodo_entrega IN ('PRESENCIAL', 'PROCURACAO', 'CORREIO')
    )
);

CREATE INDEX idx_diploma_curso_situacao ON diploma (id_curso, situacao);
CREATE INDEX idx_diploma_storage_key ON diploma (storage_key);
CREATE INDEX idx_diploma_data_colacao ON diploma (data_colacao DESC);
