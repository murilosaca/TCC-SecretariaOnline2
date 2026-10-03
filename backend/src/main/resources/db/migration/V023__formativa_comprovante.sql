-- F1.11: comprovante de formativa manual e tipos elegíveis por curso.
ALTER TABLE formativa ADD COLUMN storage_key VARCHAR(512);
ALTER TABLE formativa ADD COLUMN id_tipo_atividade UUID;

CREATE TABLE tipo_atividade_formativa (
    id          UUID            PRIMARY KEY,
    id_curso    UUID            NOT NULL REFERENCES curso (id),
    nome        VARCHAR(200)    NOT NULL,
    ativo       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ     NOT NULL,
    updated_at  TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uk_tipo_atividade_curso_nome UNIQUE (id_curso, nome)
);

CREATE INDEX idx_tipo_atividade_curso ON tipo_atividade_formativa (id_curso);

ALTER TABLE formativa
    ADD CONSTRAINT fk_formativa_tipo_atividade
    FOREIGN KEY (id_tipo_atividade) REFERENCES tipo_atividade_formativa (id);
