-- Estágio registrado pela secretaria (RF-F1-007 / RF-F3-005).
-- PDF em bytea nesta fatia, como certificado. MinIO/S3 continua fora (sem modules/arquivos).
-- id_orientador aponta para usuario IAM sem FK cross-módulo (mesmo critério de curso.id_coordenador).

CREATE TABLE estagio (
    id              UUID            PRIMARY KEY,
    id_aluno        UUID            NOT NULL REFERENCES aluno (id),
    id_curso        UUID            NOT NULL REFERENCES curso (id),
    id_orientador   UUID            NOT NULL,
    empresa         VARCHAR(200)    NOT NULL,
    supervisor      VARCHAR(200)    NOT NULL,
    inicio          DATE            NOT NULL,
    fim             DATE            NOT NULL,
    situacao        VARCHAR(20)     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_estagio_aluno ON estagio (id_aluno);
CREATE INDEX idx_estagio_orientador ON estagio (id_orientador);
CREATE INDEX idx_estagio_orientador_situacao ON estagio (id_orientador, situacao);

CREATE TABLE estagio_documento (
    id              UUID            PRIMARY KEY,
    id_estagio      UUID            NOT NULL REFERENCES estagio (id),
    tipo            VARCHAR(40)     NOT NULL,
    obrigatorio     BOOLEAN         NOT NULL,
    estado          VARCHAR(40)     NOT NULL,
    nome_arquivo    VARCHAR(255),
    content_type    VARCHAR(100),
    conteudo        BYTEA,
    tamanho         INTEGER,
    enviado_em      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uq_estagio_documento_tipo UNIQUE (id_estagio, tipo)
);

CREATE INDEX idx_estagio_documento_estagio ON estagio_documento (id_estagio);

CREATE TABLE estagio_parecer (
    id              UUID            PRIMARY KEY,
    id_documento    UUID            NOT NULL REFERENCES estagio_documento (id),
    id_autor        UUID            NOT NULL,
    acao            VARCHAR(20)     NOT NULL,
    texto           VARCHAR(2000)   NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_estagio_parecer_documento ON estagio_parecer (id_documento);
CREATE INDEX idx_estagio_parecer_criado ON estagio_parecer (created_at DESC);
