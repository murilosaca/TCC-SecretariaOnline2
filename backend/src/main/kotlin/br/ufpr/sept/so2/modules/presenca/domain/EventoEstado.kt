package br.ufpr.sept.so2.modules.presenca.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class EventoEstado {
    AGENDADO,
    EM_ANDAMENTO,
    CONCLUIDO,
    ;

    companion object {
        fun from(raw: String?): EventoEstado {
            if (raw.isNullOrBlank()) {
                throw DadoInvalidoException("Estado do evento obrigatório.")
            }
            return try {
                valueOf(raw.trim().uppercase())
            } catch (_: IllegalArgumentException) {
                throw DadoInvalidoException("Estado do evento inválido.")
            }
        }
    }
}
