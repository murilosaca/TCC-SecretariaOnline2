package br.ufpr.sept.so2.modules.tcc.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class TccSituacao {
    ATIVO,
    CONCLUIDO,
    ;

    companion object {
        fun from(valor: String?): TccSituacao {
            val normalizado = valor?.trim()?.uppercase().orEmpty()
            return entries.find { it.name == normalizado }
                ?: throw DadoInvalidoException("Situação de TCC inválida.")
        }
    }
}
