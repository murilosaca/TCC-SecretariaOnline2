-- F1.6 / F3.8: hub de comunicação e audiência do professor (turma ou curso).
CREATE TABLE turma (
    id              UUID            PRIMARY KEY,
    id_curso        UUID            NOT NULL REFERENCES curso (id),
    id_professor    UUID            NOT NULL REFERENCES usuario (id),
    codigo          VARCHAR(30)     NOT NULL,
    nome            VARCHAR(200)    NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uk_turma_professor_codigo UNIQUE (id_professor, codigo)
);

CREATE INDEX idx_turma_professor ON turma (id_professor);
CREATE INDEX idx_turma_curso ON turma (id_curso);

CREATE TABLE turma_aluno (
    id_turma    UUID    NOT NULL REFERENCES turma (id),
    id_aluno    UUID    NOT NULL REFERENCES aluno (id),
    PRIMARY KEY (id_turma, id_aluno)
);

CREATE TABLE comunicacao (
    id               UUID            PRIMARY KEY,
    tipo             VARCHAR(20)     NOT NULL,
    titulo           VARCHAR(200)    NOT NULL,
    corpo            TEXT            NOT NULL,
    prioridade       VARCHAR(20)     NOT NULL,
    id_autor         UUID            NOT NULL REFERENCES usuario (id),
    audiencia_tipo   VARCHAR(20)     NOT NULL,
    audiencia_id     UUID            NOT NULL,
    expires_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ     NOT NULL,
    updated_at       TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_comunicacao_autor ON comunicacao (id_autor, created_at DESC);

CREATE TABLE comunicacao_entrega (
    id                  UUID            PRIMARY KEY,
    id_comunicacao      UUID            NOT NULL REFERENCES comunicacao (id),
    id_destinatario     UUID            NOT NULL REFERENCES usuario (id),
    read_at             TIMESTAMPTZ,
    acao_href           VARCHAR(300),
    in_app              BOOLEAN         NOT NULL DEFAULT TRUE,
    email_enviado       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uk_comunicacao_destinatario UNIQUE (id_comunicacao, id_destinatario)
);

CREATE INDEX idx_comunicacao_entrega_destinatario ON comunicacao_entrega (id_destinatario, created_at DESC);
