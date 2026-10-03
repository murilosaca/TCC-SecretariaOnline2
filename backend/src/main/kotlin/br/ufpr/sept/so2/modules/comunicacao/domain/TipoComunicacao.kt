package br.ufpr.sept.so2.modules.comunicacao.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class TipoComunicacao {
    INSTITUCIONAL,
    TURMA,
    INBOX,
    ;

    companion object {
        fun parseFiltro(raw: String?): TipoComunicacao {
            val texto = raw?.trim()?.uppercase().orEmpty()
            return entries.find { it.name == texto }
                ?: throw DadoInvalidoException("Tipo de comunicação inválido.")
        }
    }
}
