package br.ufpr.sept.so2.modules.iam.domain

import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr

/**
 * Identificador aceito no login: e-mail institucional/pessoal ou GRR (RF-F0-001-a).
 */
data class IdentificadorLogin private constructor(
    val tipo: Tipo,
    val valor: String,
) {
    enum class Tipo {
        EMAIL,
        GRR,
    }

    fun mascarado(): String {
        if (tipo == Tipo.GRR) {
            if (valor.length < 7) {
                return "GRR****"
            }
            return "GRR****" + valor.substring(valor.length - 4)
        }
        val at = valor.indexOf('@')
        if (at <= 1) {
            return "***" + valor.substring(maxOf(at, 0))
        }
        return valor[0] + "***" + valor.substring(at)
    }

    companion object {
        private val SO_DIGITOS = Regex("^\\d{8}$")

        fun tryParse(raw: String?): IdentificadorLogin? {
            val trimmed = raw?.trim().orEmpty()
            if (trimmed.isEmpty()) {
                return null
            }
            return try {
                if (trimmed.contains("@")) {
                    IdentificadorLogin(Tipo.EMAIL, Email.of(trimmed).value)
                } else {
                    val candidato = if (SO_DIGITOS.matches(trimmed)) "GRR$trimmed" else trimmed
                    IdentificadorLogin(Tipo.GRR, Grr.of(candidato).value)
                }
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}
