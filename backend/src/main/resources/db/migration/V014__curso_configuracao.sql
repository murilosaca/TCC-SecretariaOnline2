-- F6.1 (RF-F6-001): parâmetros da coordenação, fora do CRUD da secretaria.
-- Horas formativas mínimas continuam em curso.horas_formativas_minimas.
-- Não cria período letivo por curso (F5.9) e não desvincula coordenador.

CREATE TABLE curso_configuracao (
    id                       UUID            PRIMARY KEY REFERENCES curso (id),
    duracao_calendario       SMALLINT        NOT NULL DEFAULT 15,
    banca_membros_externos   SMALLINT        NOT NULL DEFAULT 1,
    banca_modalidade         VARCHAR(20)     NOT NULL DEFAULT 'PRESENCIAL',
    regimento                VARCHAR(10000)  NOT NULL DEFAULT '',
    created_at               TIMESTAMPTZ     NOT NULL,
    updated_at               TIMESTAMPTZ     NOT NULL,
    CONSTRAINT ck_curso_config_calendario CHECK (duracao_calendario IN (15, 18)),
    CONSTRAINT ck_curso_config_membros CHECK (banca_membros_externos IN (1, 2)),
    CONSTRAINT ck_curso_config_modalidade CHECK (banca_modalidade IN ('PRESENCIAL', 'REMOTO', 'HIBRIDO'))
);

-- Quem já atingiu o limiar anterior permanece elegível quando o limiar sobe (RN-F6-001-07).
-- Sem linha, o cálculo novo usa curso.horas_formativas_minimas.
CREATE TABLE elegibilidade_horas (
    id                UUID            PRIMARY KEY REFERENCES aluno (id),
    id_curso          UUID            NOT NULL REFERENCES curso (id),
    elegivel          BOOLEAN         NOT NULL,
    limiar_aplicado   INTEGER         NOT NULL,
    horas_validadas   INTEGER         NOT NULL,
    congelada_em      TIMESTAMPTZ     NOT NULL,
    created_at        TIMESTAMPTZ     NOT NULL,
    updated_at        TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_elegibilidade_horas_curso ON elegibilidade_horas (id_curso);
