-- F5.6-D02/D05: vínculo do aluno com o usuário IAM, vagas e matrícula no período.
-- Flyway imutável: não altera V001–V034.

ALTER TABLE aluno ADD COLUMN id_usuario UUID;
ALTER TABLE aluno ADD CONSTRAINT uk_aluno_usuario UNIQUE (id_usuario);

ALTER TABLE disciplina ADD COLUMN vagas INTEGER NOT NULL DEFAULT 0;
ALTER TABLE disciplina ADD CONSTRAINT ck_disciplina_vagas CHECK (vagas >= 0);

CREATE TABLE matricula (
    id                  UUID            PRIMARY KEY,
    id_aluno            UUID            NOT NULL REFERENCES aluno (id),
    id_disciplina       UUID            NOT NULL REFERENCES disciplina (id),
    id_periodo_letivo   UUID            NOT NULL REFERENCES periodo_letivo (id),
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uk_matricula_aluno_disciplina_periodo UNIQUE (id_aluno, id_disciplina, id_periodo_letivo)
);

CREATE INDEX idx_matricula_aluno ON matricula (id_aluno);
CREATE INDEX idx_matricula_disciplina ON matricula (id_disciplina);
