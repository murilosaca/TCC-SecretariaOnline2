CREATE TABLE curso (
    id                          UUID            PRIMARY KEY,
    nome                        VARCHAR(200)    NOT NULL,
    sigla                       VARCHAR(20)     NOT NULL UNIQUE,
    codigo                      VARCHAR(30)     NOT NULL UNIQUE,
    id_coordenador              UUID,
    horas_formativas_minimas    INTEGER         NOT NULL DEFAULT 120,
    ativo                       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMPTZ     NOT NULL,
    updated_at                  TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_curso_coordenador ON curso (id_coordenador);

CREATE TABLE disciplina (
    id                      UUID            PRIMARY KEY,
    id_curso                UUID            NOT NULL REFERENCES curso (id),
    codigo                  VARCHAR(20)     NOT NULL,
    nome                    VARCHAR(200)    NOT NULL,
    periodo                 INTEGER         NOT NULL,
    carga_horaria_total     INTEGER         NOT NULL,
    creditos                INTEGER         NOT NULL,
    ativa                   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ     NOT NULL,
    updated_at              TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uk_disciplina_curso_codigo UNIQUE (id_curso, codigo)
);

CREATE INDEX idx_disciplina_curso ON disciplina (id_curso);

CREATE TABLE aluno (
    id                      UUID            PRIMARY KEY,
    nome                    VARCHAR(200)    NOT NULL,
    nome_social             VARCHAR(200),
    grr                     VARCHAR(11)     NOT NULL UNIQUE,
    email_institucional     CITEXT          NOT NULL UNIQUE,
    email_pessoal           CITEXT,
    telefone                VARCHAR(30),
    id_curso                UUID            NOT NULL REFERENCES curso (id),
    situacao                VARCHAR(20)     NOT NULL,
    ativo                   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ     NOT NULL,
    updated_at              TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_aluno_curso ON aluno (id_curso);
CREATE INDEX idx_aluno_situacao ON aluno (situacao);

CREATE TABLE periodo_letivo (
    id          UUID            PRIMARY KEY,
    ano         SMALLINT        NOT NULL,
    semestre    SMALLINT        NOT NULL,
    inicio      DATE            NOT NULL,
    fim         DATE            NOT NULL,
    ativo       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ     NOT NULL,
    updated_at  TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uk_periodo_ano_semestre UNIQUE (ano, semestre),
    CONSTRAINT ck_periodo_semestre CHECK (semestre IN (1, 2)),
    CONSTRAINT ck_periodo_intervalo CHECK (fim > inicio)
);
