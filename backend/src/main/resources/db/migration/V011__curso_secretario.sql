-- RF-F5-004-a / F5.7: secretários N:N do curso (escopo por curso).
-- id_usuario é o usuario IAM; sem FK cross-módulo. Índice para montar o escopo.

CREATE TABLE curso_secretario (
    id_curso    UUID    NOT NULL REFERENCES curso (id) ON DELETE CASCADE,
    id_usuario  UUID    NOT NULL,
    PRIMARY KEY (id_curso, id_usuario)
);

CREATE INDEX idx_curso_secretario_usuario ON curso_secretario (id_usuario);
