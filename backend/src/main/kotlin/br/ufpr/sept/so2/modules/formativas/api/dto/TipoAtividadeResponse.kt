package br.ufpr.sept.so2.modules.formativas.api.dto

import br.ufpr.sept.so2.modules.formativas.domain.TipoAtividadeFormativa
import java.util.UUID

data class TipoAtividadeResponse(
    val id: UUID,
    val nome: String,
)

data class TiposAtividadeResponse(
    val content: List<TipoAtividadeResponse>,
) {
    companion object {
        fun from(tipos: List<TipoAtividadeFormativa>) = TiposAtividadeResponse(
            tipos.map { TipoAtividadeResponse(it.id, it.nome) },
        )
    }
}
