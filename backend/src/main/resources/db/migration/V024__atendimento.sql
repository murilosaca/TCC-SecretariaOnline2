-- F5.13 + F1.20: registro imutável de atendimento e ciência do aluno.
CREATE TABLE atendimento_categoria (
    id          UUID            PRIMARY KEY,
    nome        VARCHAR(120)    NOT NULL,
    ativo       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ     NOT NULL,
    updated_at  TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uk_atendimento_categoria_nome UNIQUE (nome)
);

CREATE TABLE atendimento (
    id              UUID            PRIMARY KEY,
    id_aluno        UUID            NOT NULL REFERENCES aluno (id),
    id_categoria    UUID            NOT NULL REFERENCES atendimento_categoria (id),
    id_registrador  UUID            NOT NULL REFERENCES usuario (id),
    assunto         VARCHAR(200)    NOT NULL,
    resposta        TEXT            NOT NULL,
    storage_key     VARCHAR(512),
    estado          VARCHAR(20)     NOT NULL,
    ciencia_em      TIMESTAMPTZ,
    ciencia_ip      VARCHAR(64),
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT ck_atendimento_estado CHECK (estado IN ('PENDENTE_CIENCIA', 'CIENCIA_DADA')),
    CONSTRAINT ck_atendimento_ciencia CHECK (
        (estado = 'PENDENTE_CIENCIA' AND ciencia_em IS NULL)
        OR (estado = 'CIENCIA_DADA' AND ciencia_em IS NOT NULL)
    )
);

CREATE INDEX idx_atendimento_aluno_estado ON atendimento (id_aluno, estado);
CREATE INDEX idx_atendimento_created_at ON atendimento (created_at DESC);
