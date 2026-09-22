package br.ufpr.sept.so2.modules.tcc.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class PapelBancaTcc {
    ORIENTADOR,
    COORIENTADOR,
    BANCA,
    ;

    companion object {
        fun from(valor: String?): PapelBancaTcc {
            val normalizado = valor?.trim()?.uppercase()?.replace('-', '_')?.replace(' ', '_').orEmpty()
            return entries.find { it.name == normalizado }
                ?: throw DadoInvalidoException("Papel de banca inválido.")
        }
    }
}
