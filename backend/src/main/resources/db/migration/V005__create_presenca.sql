CREATE TABLE evento (
    id                      UUID            PRIMARY KEY,
    titulo                  VARCHAR(200)    NOT NULL,
    inicio_em               TIMESTAMPTZ     NOT NULL,
    fim_em                  TIMESTAMPTZ     NOT NULL,
    carga_horaria           INTEGER         NOT NULL,
    attendance_mode         VARCHAR(20)     NOT NULL,
    estado                  VARCHAR(20)     NOT NULL,
    pin_hash                VARCHAR(255),
    janela_entrada_inicio   TIMESTAMPTZ,
    janela_entrada_fim      TIMESTAMPTZ,
    janela_saida_inicio     TIMESTAMPTZ,
    janela_saida_fim        TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL,
    updated_at              TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_evento_estado_inicio ON evento (estado, inicio_em);

CREATE TABLE presenca (
    id              UUID            PRIMARY KEY,
    evento_id       UUID            NOT NULL REFERENCES evento (id),
    usuario_id      UUID            NOT NULL,
    fase            VARCHAR(20)     NOT NULL,
    device_uuid     VARCHAR(64)     NOT NULL,
    instante        TIMESTAMPTZ     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uq_presenca_evento_usuario_fase UNIQUE (evento_id, usuario_id, fase)
);

CREATE INDEX idx_presenca_evento ON presenca (evento_id);
CREATE INDEX idx_presenca_usuario ON presenca (usuario_id);
CREATE INDEX idx_presenca_evento_device ON presenca (evento_id, device_uuid);
