package br.ufpr.sept.so2.shared.domain

/**
 * Tira senha em claro de texto que pode ir para HTTP ou log.
 * O hash Argon2id continua só na coluna senha_hash.
 */
object SegredoRedigido {
    private val CAMPO = Regex(
        """"(senha|senhaTemporaria|password|senhaHash)"\s*:\s*"(?:\\.|[^"\\])*"""",
    )

    fun json(payload: String?): String {
        if (payload.isNullOrBlank()) {
            return ""
        }
        return CAMPO.replace(payload) { "\"${it.groupValues[1]}\":\"[redigido]\"" }
    }
}
