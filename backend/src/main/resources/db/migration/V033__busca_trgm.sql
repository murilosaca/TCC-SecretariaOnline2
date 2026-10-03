-- Fatia 35: GIN com pg_trgm (extensão criada na V001) para a busca global.
-- Os testes H2 não passam pelo Flyway e usam LIKE.

CREATE INDEX idx_aluno_nome_trgm
    ON aluno USING gin (lower(nome) gin_trgm_ops);

CREATE INDEX idx_evento_titulo_trgm
    ON evento USING gin (lower(titulo) gin_trgm_ops);

CREATE INDEX idx_usuario_nome_trgm
    ON usuario USING gin (lower(nome) gin_trgm_ops);

CREATE INDEX idx_solicitacao_protocolo_trgm
    ON solicitacao USING gin (lower(protocolo) gin_trgm_ops);
