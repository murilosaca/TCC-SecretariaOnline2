-- Pool COE (RF-F4-002 / F4.2): membro por curso e estágio sem orientador.
-- id_usuario é o usuario IAM; sem FK cross-módulo (mesmo critério de curso_secretario).
-- Tabela específica da COE: filtro CAAF por comissão continua dívida.

ALTER TABLE estagio ALTER COLUMN id_orientador DROP NOT NULL;

CREATE TABLE coe_membro (
    id_curso    UUID    NOT NULL REFERENCES curso (id) ON DELETE CASCADE,
    id_usuario  UUID    NOT NULL,
    PRIMARY KEY (id_curso, id_usuario)
);

CREATE INDEX idx_coe_membro_usuario ON coe_membro (id_usuario);
CREATE INDEX idx_estagio_curso_pool ON estagio (id_curso, situacao, id_orientador);
