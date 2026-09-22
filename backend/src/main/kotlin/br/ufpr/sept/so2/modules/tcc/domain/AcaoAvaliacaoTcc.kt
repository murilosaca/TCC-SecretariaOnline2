package br.ufpr.sept.so2.modules.tcc.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class AcaoAvaliacaoTcc {
    APROVAR,
    INDEFERIR,
    SOLICITAR_CORRECOES,
    ;

    fun resultado(): TccEstado = when (this) {
        APROVAR -> TccEstado.APROVADO
        INDEFERIR -> TccEstado.REPROVADO
        SOLICITAR_CORRECOES -> TccEstado.CORRECOES_SOLICITADAS
    }

    fun exigeParecerLongo(): Boolean = this != APROVAR

    companion object {
        fun from(valor: String?): AcaoAvaliacaoTcc {
            val normalizado = valor?.trim()?.uppercase()?.replace('-', '_')?.replace(' ', '_').orEmpty()
            return when (normalizado) {
                "APROVAR" -> APROVAR
                "INDEFERIR", "INDEFER", "REPROVAR" -> INDEFERIR
                "SOLICITAR_CORRECOES", "CORRECOES" -> SOLICITAR_CORRECOES
                else -> throw DadoInvalidoException("Ação de avaliação inválida.")
            }
        }
    }
}
