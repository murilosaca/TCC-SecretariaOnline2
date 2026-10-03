package br.ufpr.sept.so2.modules.solicitacoes.infrastructure

import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao
import java.time.OffsetDateTime
import java.util.UUID

class AutorizacaoImagemSeed private constructor() {
    companion object {
        @JvmField
        val ID: UUID = UUID.fromString("018f0000-0000-7000-8000-000000000028")

        @JvmField
        val CODIGO: String = "AUTORIZACAO_IMAGEM"

        @JvmField
        val FORM_SCHEMA: String = """
            {
              "type": "object",
              "title": "Autorização de uso de imagem",
              "required": ["declaracao"],
              "additionalProperties": false,
              "properties": {
                "declaracao": {
                  "type": "string",
                  "title": "Declaração",
                  "minLength": 5,
                  "maxLength": 500
                }
              }
            }
            """.trimIndent()

        @JvmField
        val WORKFLOW_JSON: String = """
            {
              "initial": "ABERTA",
              "states": {
                "ABERTA": {
                  "on": {
                    "DEFER": "DEFERIDA",
                    "INDEFER": "INDEFERIDA"
                  }
                },
                "DEFERIDA": { "on": {} },
                "INDEFERIDA": { "on": {} }
              }
            }
            """.trimIndent()

        @JvmStatic
        fun tipo(agora: OffsetDateTime): TipoSolicitacao =
            TipoSolicitacao(
                ID,
                CODIGO,
                "Autorização de uso de imagem",
                "Consentimento do aluno para uso de imagem institucional.",
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
