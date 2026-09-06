CREATE TABLE tipo_solicitacao (
    id              UUID            PRIMARY KEY,
    codigo          VARCHAR(80)     NOT NULL UNIQUE,
    nome            VARCHAR(200)    NOT NULL,
    descricao       TEXT,
    status          VARCHAR(20)     NOT NULL,
    form_schema     JSONB           NOT NULL,
    workflow_json   JSONB           NOT NULL,
    prazo_dias      INTEGER         NOT NULL DEFAULT 15,
    versao          INTEGER         NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);

CREATE TABLE solicitacao_protocolo_seq (
    ano     INTEGER PRIMARY KEY,
    ultimo  INTEGER NOT NULL
);

CREATE TABLE solicitacao (
    id                      UUID            PRIMARY KEY,
    tipo_id                 UUID            NOT NULL REFERENCES tipo_solicitacao (id),
    tipo_codigo             VARCHAR(80)     NOT NULL,
    tipo_nome               VARCHAR(200)    NOT NULL,
    tipo_versao             INTEGER         NOT NULL,
    solicitante_id          UUID            NOT NULL REFERENCES usuario (id),
    protocolo               VARCHAR(32)     NOT NULL UNIQUE,
    estado                  VARCHAR(40)     NOT NULL,
    payload                 JSONB           NOT NULL,
    form_schema_snapshot    JSONB           NOT NULL,
    workflow_snapshot       JSONB           NOT NULL,
    prazo_em                TIMESTAMPTZ     NOT NULL,
    hash_sha256             VARCHAR(64),
    created_at              TIMESTAMPTZ     NOT NULL,
    updated_at              TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_solicitacao_solicitante ON solicitacao (solicitante_id, created_at DESC);
CREATE INDEX idx_solicitacao_estado ON solicitacao (estado);
CREATE INDEX idx_solicitacao_tipo ON solicitacao (tipo_codigo);

CREATE TABLE solicitacao_evento (
    id              UUID            PRIMARY KEY,
    solicitacao_id  UUID            NOT NULL REFERENCES solicitacao (id),
    tipo            VARCHAR(80)     NOT NULL,
    estado_de       VARCHAR(40),
    estado_para     VARCHAR(40)     NOT NULL,
    ator_id         UUID,
    parecer         TEXT,
    payload         TEXT,
    created_at      TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_solicitacao_evento_solicitacao
    ON solicitacao_evento (solicitacao_id, created_at DESC);

INSERT INTO tipo_solicitacao (
    id, codigo, nome, descricao, status, form_schema, workflow_json, prazo_dias, versao, created_at, updated_at
) VALUES (
    '018f0000-0000-7000-8000-000000000001',
    'DECLARACAO_SIMPLES',
    'Declaração simples',
    'Declaração institucional de vínculo ou situação acadêmica.',
    'PUBLISHED',
    '{
      "type": "object",
      "title": "Declaração simples",
      "required": ["finalidade"],
      "additionalProperties": false,
      "properties": {
        "finalidade": {
          "type": "string",
          "title": "Finalidade da declaração",
          "minLength": 5,
          "maxLength": 200
        },
        "observacao": {
          "type": "string",
          "title": "Observação",
          "maxLength": 1000
        }
      }
    }'::jsonb,
    '{
      "initial": "EM_ANALISE",
      "states": {
        "EM_ANALISE": {
          "on": {
            "DEFER": "DELIBERADA",
            "INDEFER": "INDEFERIDA",
            "REQUEST_ADJUST": "EM_AJUSTE"
          }
        },
        "EM_AJUSTE": { "on": { "RESUBMIT": "EM_ANALISE" } },
        "DELIBERADA": { "on": { "CLOSE": "CONCLUIDA" } },
        "INDEFERIDA": { "on": {} },
        "CONCLUIDA": { "on": {} }
      }
    }'::jsonb,
    15,
    1,
    TIMESTAMPTZ '2026-01-01T00:00:00Z',
    TIMESTAMPTZ '2026-01-01T00:00:00Z'
);
