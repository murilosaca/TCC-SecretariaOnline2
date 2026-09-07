package br.ufpr.sept.so2.modules.presenca.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class FasePresenca {
    ENTRADA,
    SAIDA,
    ;

    companion object {
        fun from(raw: String?): FasePresenca {
            if (raw.isNullOrBlank()) {
                throw DadoInvalidoException("Fase de presença obrigatória.")
            }
            return try {
                valueOf(raw.trim().uppercase())
            } catch (_: IllegalArgumentException) {
                throw DadoInvalidoException("Fase de presença inválida.")
            }
        }
    }
}
