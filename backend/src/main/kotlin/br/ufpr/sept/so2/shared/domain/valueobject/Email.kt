package br.ufpr.sept.so2.shared.domain.valueobject

data class Email private constructor(val value: String) {

    fun isInstitutional(): Boolean = value.endsWith("@ufpr.br")

    override fun toString(): String = value

    companion object {
        const val PATTERN = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
        const val PATTERN_OR_BLANK = "^$|$PATTERN"
        private val EMAIL_REGEX = Regex(PATTERN)

        @JvmStatic
        fun of(raw: String?): Email {
            val normalized = raw?.trim()?.lowercase().orEmpty()
            require(EMAIL_REGEX.matches(normalized)) {
                "Formato de email inválido: $raw"
            }
            return Email(normalized)
        }
    }
}
