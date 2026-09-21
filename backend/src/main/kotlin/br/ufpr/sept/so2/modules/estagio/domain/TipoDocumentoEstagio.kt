package br.ufpr.sept.so2.modules.estagio.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class TipoDocumentoEstagio {
    TCE,
    RELATORIO_FINAL,
    ;

    companion object {
        fun from(raw: String?): TipoDocumentoEstagio {
            val nome = raw?.trim()?.uppercase()?.replace('-', '_')?.replace(' ', '_').orEmpty()
            return entries.find { it.name == nome }
                ?: throw DadoInvalidoException("Tipo de documento de estágio inválido.")
        }
    }
}
