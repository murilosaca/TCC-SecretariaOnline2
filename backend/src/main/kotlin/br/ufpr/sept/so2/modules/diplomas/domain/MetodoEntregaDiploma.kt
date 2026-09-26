package br.ufpr.sept.so2.modules.diplomas.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class MetodoEntregaDiploma {
    PRESENCIAL,
    PROCURACAO,
    CORREIO,
    ;

    companion object {
        fun from(raw: String?): MetodoEntregaDiploma {
            if (raw.isNullOrBlank()) {
                throw DadoInvalidoException("Modalidade de entrega é obrigatória.")
            }
            return try {
                valueOf(raw.trim().uppercase())
            } catch (_: IllegalArgumentException) {
                throw DadoInvalidoException("Modalidade de entrega inválida: $raw")
            }
        }
    }
}
