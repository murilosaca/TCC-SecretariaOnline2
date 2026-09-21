package br.ufpr.sept.so2.modules.estagio.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class AcaoParecerEstagio {
    APROVAR,
    REPROVAR,
    ;

    fun resultado(): String = if (this == APROVAR) "APROVADO" else "REPROVADO"

    companion object {
        fun from(raw: String?): AcaoParecerEstagio {
            val nome = raw?.trim()?.uppercase().orEmpty()
            return when (nome) {
                "APROVAR" -> APROVAR
                "REPROVAR", "INDEFERIR" -> REPROVAR
                else -> throw DadoInvalidoException("Ação de parecer inválida.")
            }
        }
    }
}
