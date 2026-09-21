package br.ufpr.sept.so2.modules.estagio.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class EstagioSituacao {
    ATIVO,
    PENDENTE,
    CONCLUIDO,
    ;

    companion object {
        fun from(raw: String?): EstagioSituacao {
            val nome = raw?.trim()?.uppercase().orEmpty()
            return entries.find { it.name == nome }
                ?: throw DadoInvalidoException("Situação de estágio inválida.")
        }
    }
}
