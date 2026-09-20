-- Parecer e revisor da revisão individual CAAF (RF-F3-004).
-- Aditivo: não altera colunas existentes nem o UNIQUE de V007.

ALTER TABLE formativa
    ADD COLUMN parecer     VARCHAR(2000),
    ADD COLUMN id_revisor  UUID,
    ADD COLUMN reviewed_at TIMESTAMPTZ;

CREATE INDEX idx_formativa_estado ON formativa (estado);
