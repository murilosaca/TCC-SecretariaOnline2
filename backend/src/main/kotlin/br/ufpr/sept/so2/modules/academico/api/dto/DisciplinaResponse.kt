package br.ufpr.sept.so2.modules.academico.api.dto

import br.ufpr.sept.so2.modules.academico.domain.Disciplina
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class DisciplinaResponse(
    val id: UUID,
    val idCurso: UUID,
    val codigo: String,
    val nome: String,
    val periodo: Int,
    val cargaHorariaTotal: Int,
    val creditos: Int,
    val ativa: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun from(disciplina: Disciplina): DisciplinaResponse {
            val base = "/academico/disciplinas/${disciplina.id}"
            return DisciplinaResponse(
                id = disciplina.id,
                idCurso = disciplina.idCurso,
                codigo = disciplina.codigo,
                nome = disciplina.nome,
                periodo = disciplina.periodo,
                cargaHorariaTotal = disciplina.cargaHorariaTotal,
                creditos = disciplina.creditos,
                ativa = disciplina.ativa,
                createdAt = disciplina.createdAt,
                updatedAt = disciplina.updatedAt,
                links = mapOf("self" to base, "atualizar" to base, "excluir" to base),
            )
        }
    }
}
