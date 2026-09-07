package br.ufpr.sept.so2.modules.solicitacoes.infrastructure

import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao
import java.time.OffsetDateTime
import java.util.UUID

class DeclaracaoSimplesSeed private constructor() {

    companion object {
        @JvmField
        val ID: UUID = UUID.fromString("018f0000-0000-7000-8000-000000000001")

        @JvmField
        val CODIGO: String = "DECLARACAO_SIMPLES"

        @JvmField
        val FORM_SCHEMA: String = """
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
            """.trimIndent()

        @JvmField
        val WORKFLOW_JSON: String = """
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
            """.trimIndent()

        @JvmStatic
        fun tipo(agora: OffsetDateTime): TipoSolicitacao =
            TipoSolicitacao(
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
                agora,
            )
    }
}
