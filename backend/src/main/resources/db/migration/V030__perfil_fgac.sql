-- Fatia 32: perfil agregador de authorities. usuario_authority permanece
-- como conjunto materializado (união dos perfis) para o JWT hasAuthority.

CREATE TABLE authority (
    nome            VARCHAR(80)     PRIMARY KEY,
    descricao       VARCHAR(300)    NOT NULL,
    modulo          VARCHAR(40)     NOT NULL,
    sistema         BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);

CREATE TABLE perfil (
    id              UUID            PRIMARY KEY,
    nome            VARCHAR(80)     NOT NULL UNIQUE,
    descricao       VARCHAR(300),
    tipo            VARCHAR(20)     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);

CREATE TABLE perfil_authority (
    perfil_id       UUID            NOT NULL REFERENCES perfil (id) ON DELETE CASCADE,
    authority       VARCHAR(80)     NOT NULL REFERENCES authority (nome),
    PRIMARY KEY (perfil_id, authority)
);

CREATE TABLE usuario_perfil (
    usuario_id      UUID            NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    perfil_id       UUID            NOT NULL REFERENCES perfil (id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, perfil_id)
);

CREATE INDEX idx_usuario_perfil_perfil ON usuario_perfil (perfil_id);
