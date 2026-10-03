-- F5.14: recorte do evento por curso vinculado à secretaria.
-- Nulo mantém o evento criado pelo professor (que não escolhe curso) funcionando.
ALTER TABLE evento ADD COLUMN id_curso UUID REFERENCES curso (id);

CREATE INDEX idx_evento_curso_estado ON evento (id_curso, estado);
