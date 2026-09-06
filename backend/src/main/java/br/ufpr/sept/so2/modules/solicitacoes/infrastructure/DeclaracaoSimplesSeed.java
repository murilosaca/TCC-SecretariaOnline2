package br.ufpr.sept.so2.modules.solicitacoes.infrastructure;

import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class DeclaracaoSimplesSeed {

    public static final UUID ID = UUID.fromString("018f0000-0000-7000-8000-000000000001");
    public static final String CODIGO = "DECLARACAO_SIMPLES";

    public static final String FORM_SCHEMA = """
            {
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
            }
            """;

    public static final String WORKFLOW_JSON = """
            {
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
            }
            """;

    private DeclaracaoSimplesSeed() {
    }

    public static TipoSolicitacao tipo(OffsetDateTime agora) {
        return new TipoSolicitacao(
                ID,
                CODIGO,
                "Declaração simples",
                "Declaração institucional de vínculo ou situação acadêmica.",
                TipoSolicitacao.PUBLISHED,
                FORM_SCHEMA,
                WORKFLOW_JSON,
                15,
                1,
                agora,
                agora
        );
    }
}
