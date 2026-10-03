-- Fatia 36: FAQ de leitura e o RequestType SUPORTE_TECNICO no motor já existente.
-- Sem support_thread. O protocolo continua PROT- do motor.

CREATE TABLE faq_item (
    id          UUID            PRIMARY KEY,
    pergunta    VARCHAR(300)    NOT NULL,
    resposta    TEXT            NOT NULL,
    ordem       INTEGER         NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL,
    updated_at  TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_faq_item_ordem ON faq_item (ordem);

INSERT INTO faq_item (id, pergunta, resposta, ordem, created_at, updated_at) VALUES
(
    '018f0000-0000-7000-8000-000000000361',
    'Como redefinir minha senha?',
    'Em /recuperar-senha informe o e-mail da conta. O link chega pelo canal já usado na recuperação de senha e vale por 24 horas.',
    1,
    TIMESTAMPTZ '2026-01-01T00:00:00Z',
    TIMESTAMPTZ '2026-01-01T00:00:00Z'
),
(
    '018f0000-0000-7000-8000-000000000362',
    'Prazo de deliberação?',
    'O prazo é o do tipo de solicitação publicado. A fila da secretaria marca o atraso quando esse prazo vence.',
    2,
    TIMESTAMPTZ '2026-01-01T00:00:00Z',
    TIMESTAMPTZ '2026-01-01T00:00:00Z'
);

INSERT INTO tipo_solicitacao (
    id, codigo, nome, descricao, status, form_schema, workflow_json, prazo_dias, versao, created_at, updated_at
) VALUES (
    '018f0000-0000-7000-8000-000000000036',
    'SUPORTE_TECNICO',
    'Suporte técnico',
    'Ticket de dúvida operacional aberto em /suporte.',
    'PUBLISHED',
    '{
      "type": "object",
      "title": "Suporte técnico",
      "required": ["assunto", "mensagem"],
      "additionalProperties": false,
      "properties": {
        "assunto": { "type": "string", "title": "Assunto", "minLength": 1, "maxLength": 200 },
        "mensagem": { "type": "string", "title": "Mensagem", "minLength": 1, "maxLength": 4000 }
      }
    }'::jsonb,
    '{
      "initial": "EM_ANALISE",
      "states": {
        "EM_ANALISE": { "on": { "DEFER": "DELIBERADA", "INDEFER": "INDEFERIDA" } },
        "DELIBERADA": { "on": { "CLOSE": "CONCLUIDA" } },
        "INDEFERIDA": { "on": {} },
        "CONCLUIDA": { "on": {} }
      }
    }'::jsonb,
    5,
    1,
    TIMESTAMPTZ '2026-01-01T00:00:00Z',
    TIMESTAMPTZ '2026-01-01T00:00:00Z'
);
