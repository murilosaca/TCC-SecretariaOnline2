ALTER TABLE solicitacao
    ADD COLUMN deliberador_id UUID REFERENCES usuario (id);

CREATE INDEX idx_solicitacao_deliberador ON solicitacao (deliberador_id);
CREATE INDEX idx_solicitacao_prazo_em ON solicitacao (prazo_em);
