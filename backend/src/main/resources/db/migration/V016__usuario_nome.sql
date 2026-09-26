-- F7.1: nome exibível e buscável (trigrama/ILIKE) em usuario.
ALTER TABLE usuario ADD COLUMN nome VARCHAR(200);

UPDATE usuario
SET nome = split_part(email_institucional::text, '@', 1)
WHERE nome IS NULL;

ALTER TABLE usuario ALTER COLUMN nome SET NOT NULL;

CREATE INDEX idx_usuario_nome ON usuario (lower(nome));
