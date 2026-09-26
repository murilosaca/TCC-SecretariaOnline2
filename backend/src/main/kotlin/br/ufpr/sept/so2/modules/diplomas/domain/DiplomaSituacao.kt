package br.ufpr.sept.so2.modules.diplomas.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class DiplomaSituacao {
    PENDENTE,
    ENTREGUE,
    ;

    companion object {
        fun from(raw: String?): DiplomaSituacao {
            if (raw.isNullOrBlank()) {
                throw DadoInvalidoException("Situação do diploma é obrigatória.")
            }
            return try {
                valueOf(raw.trim().uppercase())
            } catch (_: IllegalArgumentException) {
                throw DadoInvalidoException("Situação do diploma inválida: $raw")
            }
        }
    }
}
