package br.ufpr.sept.so2.modules.comunicacao.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class PrioridadeComunicacao {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    ;

    companion object {
        fun parse(raw: String?): PrioridadeComunicacao {
            val texto = raw?.trim()?.uppercase().orEmpty()
            return entries.find { it.name == texto }
                ?: throw DadoInvalidoException("Prioridade inválida.")
        }
    }
}
