package br.ufpr.sept.so2.modules.solicitacoes.infrastructure

import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao
import java.time.OffsetDateTime
import java.util.UUID

class SuporteTecnicoSeed private constructor() {
    companion object {
        @JvmField
        val ID: UUID = UUID.fromString("018f0000-0000-7000-8000-000000000036")

        @JvmField
        val CODIGO: String = "SUPORTE_TECNICO"

        @JvmField
        val FORM_SCHEMA: String = """
            {
              "type": "object",
              "title": "Suporte técnico",
              "required": ["assunto", "mensagem"],
              "additionalProperties": false,
              "properties": {
                "assunto": {
                  "type": "string",
                  "title": "Assunto",
                  "minLength": 1,
                  "maxLength": 200
                },
                "mensagem": {
                  "type": "string",
                  "title": "Mensagem",
                  "minLength": 1,
                  "maxLength": 4000
                }
              }
            }
            """.trimIndent()

        @JvmField
        val WORKFLOW_JSON: String = """
            {
              "initial": "EM_ANALISE",
              "states": {
                "EM_ANALISE": { "on": { "DEFER": "DELIBERADA", "INDEFER": "INDEFERIDA" } },
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
                "Suporte técnico",
                "Ticket de dúvida operacional aberto em /suporte.",
                TipoSolicitacao.PUBLISHED,
                FORM_SCHEMA,
                WORKFLOW_JSON,
                5,
                1,
                agora,
                agora,
            )
    }
}
