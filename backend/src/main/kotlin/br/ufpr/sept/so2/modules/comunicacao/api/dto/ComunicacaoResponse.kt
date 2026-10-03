package br.ufpr.sept.so2.modules.comunicacao.api.dto

import br.ufpr.sept.so2.modules.comunicacao.application.ComunicacaoItem
import br.ufpr.sept.so2.modules.comunicacao.application.ContagemNaoLidas
import br.ufpr.sept.so2.modules.comunicacao.domain.AudienciaOpcao
import br.ufpr.sept.so2.modules.comunicacao.domain.Comunicacao
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ComunicacaoItemResponse(
    val id: UUID,
    val tipo: String,
    val titulo: String,
    val corpo: String,
    val prioridade: String,
    val data: OffsetDateTime,
    val lida: Boolean,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)

data class BadgesResponse(
    val todos: Long,
    val institucional: Long,
    val turma: Long,
    val inbox: Long,
) {
    companion object {
        fun from(contagem: ContagemNaoLidas) = BadgesResponse(
            contagem.todos,
            contagem.institucional,
            contagem.turma,
            contagem.inbox,
        )
    }
}

data class AudienciaResponse(
    val tipo: String,
    val id: UUID,
    val rotulo: String,
)

data class AudienciasResponse(
    val content: List<AudienciaResponse>,
)

data class ComunicacaoPublicadaResponse(
    val id: UUID,
    val tipo: String,
    val titulo: String,
    val prioridade: String,
    val expiraEm: OffsetDateTime?,
)

object ComunicacaoAssembler {
    fun item(item: ComunicacaoItem): ComunicacaoItemResponse {
        val self = "/communications/${item.id}"
        val links = linkedMapOf("self" to self)
        if (item.podeMarcarLida) {
            links["marcar-lido"] = "$self/read"
        }
        if (!item.acaoHref.isNullOrBlank()) {
            links["acao"] = item.acaoHref
        }
        return ComunicacaoItemResponse(
            item.id,
            item.tipo.name,
            item.titulo,
            item.corpo,
            item.prioridade,
            item.data,
            item.lida,
            links,
        )
    }

    fun publicada(comunicacao: Comunicacao) = ComunicacaoPublicadaResponse(
        comunicacao.id,
        comunicacao.tipo.name,
        comunicacao.titulo,
        comunicacao.prioridade.name,
        comunicacao.expiresAt,
    )

    fun audiencia(opcao: AudienciaOpcao) = AudienciaResponse(opcao.tipo.name, opcao.id, opcao.rotulo)
}
