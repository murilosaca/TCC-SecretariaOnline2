package br.ufpr.sept.so2.modules.egresso.api.dto

import br.ufpr.sept.so2.modules.egresso.application.EgressoListado
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Item da lista F5.10. Sem `_links.novo` (não existe “Novo egresso”) e sem
 * `confirm-delivery` — a entrega continua no diploma (fatia 16).
 */
data class EgressoItemResponse(
    val alunoId: UUID,
    val nome: String,
    val grr: String?,
    val cursoId: UUID,
    val cursoSigla: String,
    val cursoNome: String?,
    val dataColacao: OffsetDateTime,
    val anoColacao: Int,
    val situacaoDiploma: String,
    val numeroDiploma: String,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun from(egresso: EgressoListado): EgressoItemResponse = EgressoItemResponse(
            egresso.alunoId,
            egresso.nome,
            egresso.grr,
            egresso.cursoId,
            egresso.cursoSigla,
            egresso.cursoNome,
            egresso.dataColacao,
            egresso.anoColacao,
            egresso.situacaoDiploma,
            egresso.numeroDiploma,
            emptyMap(),
        )
    }
}
