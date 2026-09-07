package br.ufpr.sept.so2.modules.iam.application.ports

interface PasswordHasher {
    fun hash(raw: String): String

    fun matches(raw: String?, hash: String?): Boolean

    /**
     * Equaliza o tempo de resposta quando o identificador não existe (RNF-SEC-09).
     */
    fun matchesDummy()
}
