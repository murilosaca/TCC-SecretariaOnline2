-- Pool CAAF (RF-F4-001 / F4.1): membro genérico de comissão + responsável na formativa.
-- coe_membro (V015) permanece específico da COE; CAAF usa comissao_membro com tipo=CAAF.
-- id_usuario é o usuario IAM; sem FK cross-módulo (mesmo critério de curso_secretario / coe_membro).

CREATE TABLE comissao_membro (
    id_curso    UUID         NOT NULL REFERENCES curso (id) ON DELETE CASCADE,
    id_usuario  UUID         NOT NULL,
    tipo        VARCHAR(16)  NOT NULL,
    PRIMARY KEY (id_curso, id_usuario, tipo),
    CONSTRAINT ck_comissao_membro_tipo CHECK (tipo IN ('CAAF', 'COE'))
);

CREATE INDEX idx_comissao_membro_usuario ON comissao_membro (id_usuario);
CREATE INDEX idx_comissao_membro_tipo ON comissao_membro (tipo, id_curso);

ALTER TABLE formativa ADD COLUMN id_responsavel UUID NULL;

CREATE INDEX idx_formativa_pool ON formativa (estado, id_responsavel);
CREATE INDEX idx_formativa_responsavel ON formativa (id_responsavel)
    WHERE id_responsavel IS NOT NULL;
