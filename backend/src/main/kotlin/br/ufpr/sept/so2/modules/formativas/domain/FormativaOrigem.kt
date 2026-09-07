package br.ufpr.sept.so2.modules.formativas.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class FormativaOrigem {
    PRESENCA_VALIDADA,
    COMPROVANTE,
    ;

    companion object {
        fun from(raw: String?): FormativaOrigem {
            if (raw.isNullOrBlank()) {
                throw DadoInvalidoException("Origem da formativa é obrigatória.")
            }
            return try {
                valueOf(raw.trim().uppercase())
            } catch (_: IllegalArgumentException) {
                throw DadoInvalidoException("Origem da formativa inválida.")
            }
        }
    }
}
