package br.ufpr.sept.so2.modules.estagio.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class EstadoDocumentoEstagio {
    PENDENTE,
    AGUARDANDO_PARECER,
    APROVADO,
    REPROVADO,
    ;

    fun podeEnviar(): Boolean = this == PENDENTE || this == REPROVADO

    fun podeRevisar(): Boolean = this == AGUARDANDO_PARECER

    companion object {
        fun from(raw: String?): EstadoDocumentoEstagio {
            val nome = raw?.trim()?.uppercase().orEmpty()
            return entries.find { it.name == nome }
                ?: throw DadoInvalidoException("Estado de documento de estágio inválido.")
        }
    }
}
