package br.ufpr.sept.so2.modules.formativas.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class FormativaEstado {
    PENDENTE_CONFIRMACAO,
    AGUARDANDO_CAAF,
    APROVADA,
    INDEFERIDA,
    CANCELADA,
    ;

    fun podeConfirmarOuCancelar(): Boolean = this == PENDENTE_CONFIRMACAO

    companion object {
        fun from(raw: String?): FormativaEstado {
            if (raw.isNullOrBlank()) {
                throw DadoInvalidoException("Estado da formativa é obrigatório.")
            }
            return try {
                valueOf(raw.trim().uppercase())
            } catch (_: IllegalArgumentException) {
                throw DadoInvalidoException("Estado da formativa inválido.")
            }
        }
    }
}
