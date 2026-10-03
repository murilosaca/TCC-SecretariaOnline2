package br.ufpr.sept.so2.modules.atendimentos.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class AtendimentoEstado {
    PENDENTE_CIENCIA,
    CIENCIA_DADA,
    ;

    companion object {
        fun from(raw: String?): AtendimentoEstado {
            val valor = raw?.trim()?.uppercase()
                ?: throw DadoInvalidoException(MENSAGEM)
            return entries.firstOrNull { it.name == valor } ?: throw DadoInvalidoException(MENSAGEM)
        }

        fun optional(raw: String?): AtendimentoEstado? =
            if (raw.isNullOrBlank()) null else from(raw)

        private const val MENSAGEM = "Estado de atendimento inválido."
    }
}
