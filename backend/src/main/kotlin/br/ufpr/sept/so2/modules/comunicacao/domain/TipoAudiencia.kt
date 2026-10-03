package br.ufpr.sept.so2.modules.comunicacao.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class TipoAudiencia {
    TURMA,
    CURSO,
    ;

    fun tipoComunicacao(): TipoComunicacao = when (this) {
        TURMA -> TipoComunicacao.TURMA
        CURSO -> TipoComunicacao.INSTITUCIONAL
    }

    companion object {
        fun parse(raw: String?): TipoAudiencia {
            val texto = raw?.trim()?.uppercase().orEmpty()
            return entries.find { it.name == texto }
                ?: throw DadoInvalidoException(FORA_DE_ESCOPO)
        }

        const val FORA_DE_ESCOPO = "Audiência fora do escopo."
    }
}
