package br.ufpr.sept.so2.modules.solicitacoes.api.dto

import br.ufpr.sept.so2.modules.solicitacoes.domain.SolicitacaoEvento
import java.time.OffsetDateTime
import java.util.UUID

data class SolicitacaoEventoResponse(
    val id: UUID,
    val tipo: String,
    val estadoDe: String?,
    val estadoPara: String,
    val atorId: UUID?,
    val parecer: String?,
    val createdAt: OffsetDateTime,
) {
    companion object {
        @JvmStatic
        fun from(evento: SolicitacaoEvento): SolicitacaoEventoResponse =
            SolicitacaoEventoResponse(
                evento.id,
                evento.tipo,
                evento.estadoDe,
                evento.estadoPara,
                evento.atorId,
                evento.parecer,
                evento.createdAt,
            )
    }
}
