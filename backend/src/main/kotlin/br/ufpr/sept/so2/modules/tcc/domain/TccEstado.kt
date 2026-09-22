package br.ufpr.sept.so2.modules.tcc.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class TccEstado {
    EM_ELABORACAO,
    SUBMETIDO,
    CORRECOES_SOLICITADAS,
    APROVADO,
    REPROVADO,
    ;

    fun podeEnviar(): Boolean = this == EM_ELABORACAO || this == CORRECOES_SOLICITADAS

    fun podeAvaliar(): Boolean = this == SUBMETIDO

    companion object {
        fun from(valor: String?): TccEstado {
            val normalizado = valor?.trim()?.uppercase()?.replace('-', '_')?.replace(' ', '_').orEmpty()
            return entries.find { it.name == normalizado }
                ?: throw DadoInvalidoException("Estado de TCC inválido.")
        }
    }
}
