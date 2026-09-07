-- V005 já foi aplicada no Postgres de desenvolvimento (oficina SECRET_SINGLE).
-- Anfitrião entra de forma aditiva; o seed aponta a oficina para professor.dev.
ALTER TABLE evento ADD COLUMN id_anfitriao UUID;

CREATE INDEX idx_evento_anfitriao ON evento (id_anfitriao);
