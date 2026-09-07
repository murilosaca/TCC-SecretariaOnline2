package br.ufpr.sept.so2.modules.solicitacoes.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

/**
 * Identificador público estável (RF-F0-006): `PROT-YYYY-NNNNN`.
 */
data class Protocolo private constructor(val valor: String) {

    override fun toString(): String = valor

    companion object {
        private val FORMATO = Regex("PROT-\\d{4}-\\d{5}")

        @JvmStatic
        fun of(raw: String?): Protocolo {
            val normalizado = raw?.trim()?.uppercase().orEmpty()
            if (!FORMATO.matches(normalizado)) {
                throw DadoInvalidoException("Protocolo inválido. Use o formato PROT-AAAA-NNNNN.")
            }
            return Protocolo(normalizado)
        }

        @JvmStatic
        fun formatar(ano: Int, numero: Int): Protocolo {
            if (ano < 2000 || ano > 2100) {
                throw DadoInvalidoException("Ano do protocolo inválido.")
            }
            if (numero < 1 || numero > 99999) {
                throw DadoInvalidoException("Número anual do protocolo inválido.")
            }
            return Protocolo("PROT-%d-%05d".format(ano, numero))
        }
    }
}
