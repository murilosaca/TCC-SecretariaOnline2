package br.ufpr.sept.so2.modules.academico.api.dto

import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class PeriodoLetivoResponse(
    val id: UUID,
    val ano: Int,
    val semestre: Int,
    val inicio: LocalDate,
    val fim: LocalDate,
    val ativo: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun from(periodo: PeriodoLetivo): PeriodoLetivoResponse {
            val base = "/academico/periodos/${periodo.id}"
            return PeriodoLetivoResponse(
                id = periodo.id,
                ano = periodo.ano,
                semestre = periodo.semestre,
                inicio = periodo.inicio,
                fim = periodo.fim,
                ativo = periodo.ativo,
                createdAt = periodo.createdAt,
                updatedAt = periodo.updatedAt,
                links = mapOf(
                    "self" to base,
                    "atualizar" to base,
                    "excluir" to base,
                ),
            )
        }
    }
}
