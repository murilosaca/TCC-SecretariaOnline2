package br.ufpr.sept.so2.modules.formativas.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class TipoComissao {
    CAAF,
    COE,
    ;

    companion object {
        fun from(raw: String?): TipoComissao {
            if (raw.isNullOrBlank()) {
                throw DadoInvalidoException("Tipo de comissão é obrigatório.")
            }
            return try {
                valueOf(raw.trim().uppercase())
            } catch (_: IllegalArgumentException) {
                throw DadoInvalidoException("Tipo de comissão inválido.")
            }
        }
    }
}
