package br.ufpr.sept.so2.modules.academico.api.dto

import br.ufpr.sept.so2.modules.academico.domain.Curso
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class CursoResponse(
    val id: UUID,
    val nome: String,
    val sigla: String,
    val codigo: String,
    val idCoordenador: UUID?,
    val horasFormativasMinimas: Int,
    val ativo: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun from(curso: Curso): CursoResponse {
            val base = "/academico/cursos/${curso.id}"
            return CursoResponse(
                id = curso.id,
                nome = curso.nome,
                sigla = curso.sigla,
                codigo = curso.codigo,
                idCoordenador = curso.idCoordenador,
                horasFormativasMinimas = curso.horasFormativasMinimas,
                ativo = curso.ativo,
                createdAt = curso.createdAt,
                updatedAt = curso.updatedAt,
                links = mapOf(
                    "self" to base,
                    "atualizar" to base,
                    "excluir" to base,
                    "disciplinas" to "$base/disciplinas",
                ),
            )
        }
    }
}
