-- F1.3–F1.5: dados pessoais, dispositivo da sessão e preferência de notificação.
ALTER TABLE usuario ADD COLUMN nome_social VARCHAR(120);
ALTER TABLE usuario ADD COLUMN telefone VARCHAR(20);
ALTER TABLE usuario ADD COLUMN identidade_genero VARCHAR(40);
ALTER TABLE usuario ADD COLUMN foto_storage_key VARCHAR(512);

ALTER TABLE refresh_token ADD COLUMN user_agent VARCHAR(300);

CREATE TABLE notificacao_preferencia (
    usuario_id      UUID            PRIMARY KEY REFERENCES usuario (id),
    canais          JSONB           NOT NULL,
    dnd_inicio      TIME,
    dnd_fim         TIME,
    digest          VARCHAR(20)     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);
