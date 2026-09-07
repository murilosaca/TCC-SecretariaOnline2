CREATE TABLE formativa (
    id              UUID            PRIMARY KEY,
    id_aluno        UUID            NOT NULL REFERENCES aluno (id),
    id_evento       UUID,
    origem          VARCHAR(30)     NOT NULL,
    titulo          VARCHAR(200)    NOT NULL,
    carga_horaria   INTEGER         NOT NULL,
    estado          VARCHAR(40)     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uq_formativa_evento_aluno UNIQUE (id_evento, id_aluno)
);

CREATE INDEX idx_formativa_aluno ON formativa (id_aluno);
CREATE INDEX idx_formativa_aluno_estado ON formativa (id_aluno, estado);
CREATE INDEX idx_formativa_evento ON formativa (id_evento);
