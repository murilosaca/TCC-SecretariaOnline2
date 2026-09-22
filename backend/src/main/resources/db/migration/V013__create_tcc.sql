-- TCC registrado pela secretaria (RF-F1-008 / RF-F3-006).
-- PDF da versão final em bytea nesta fatia, como estágio e certificado.
-- MinIO/S3 continua fora (sem modules/arquivos e sem URL pré-assinada).
-- id_usuario da banca aponta para usuario IAM sem FK cross-módulo
-- (mesmo critério de curso.id_coordenador e estagio.id_orientador).
-- Certificado de conclusão não é emitido aqui: elegibilidade (F6.1 / colação) está ambígua.

CREATE TABLE tcc (
    id              UUID            PRIMARY KEY,
    id_aluno        UUID            NOT NULL REFERENCES aluno (id),
    id_curso        UUID            NOT NULL REFERENCES curso (id),
    titulo          VARCHAR(200)    NOT NULL,
    situacao        VARCHAR(20)     NOT NULL,
    estado          VARCHAR(40)     NOT NULL,
    data_defesa     DATE            NOT NULL,
    data_entrega    DATE            NOT NULL,
    nome_arquivo    VARCHAR(255),
    content_type    VARCHAR(100),
    conteudo        BYTEA,
    tamanho         INTEGER,
    enviado_em      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_tcc_aluno ON tcc (id_aluno);
CREATE INDEX idx_tcc_estado ON tcc (estado);
CREATE UNIQUE INDEX uq_tcc_aluno_ativo ON tcc (id_aluno) WHERE situacao = 'ATIVO';

CREATE TABLE tcc_membro (
    id              UUID            PRIMARY KEY,
    id_tcc          UUID            NOT NULL REFERENCES tcc (id),
    id_usuario      UUID            NOT NULL,
    papel           VARCHAR(20)     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uq_tcc_membro_usuario UNIQUE (id_tcc, id_usuario)
);

CREATE INDEX idx_tcc_membro_usuario ON tcc_membro (id_usuario);

CREATE TABLE tcc_avaliacao (
    id              UUID            PRIMARY KEY,
    id_tcc          UUID            NOT NULL REFERENCES tcc (id),
    id_autor        UUID            NOT NULL,
    papel           VARCHAR(20)     NOT NULL,
    acao            VARCHAR(30)     NOT NULL,
    resultado       VARCHAR(40)     NOT NULL,
    nota            NUMERIC(3, 1)   NOT NULL,
    parecer         VARCHAR(2000)   NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_tcc_avaliacao_tcc ON tcc_avaliacao (id_tcc);
CREATE INDEX idx_tcc_avaliacao_criado ON tcc_avaliacao (created_at DESC);
