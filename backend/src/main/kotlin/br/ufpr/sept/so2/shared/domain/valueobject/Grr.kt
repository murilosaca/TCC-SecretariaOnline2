package br.ufpr.sept.so2.shared.domain.valueobject

data class Grr private constructor(val value: String) {

    override fun toString(): String = value

    companion object {
        const val PATTERN = "^GRR\\d{8}$"
        private val GRR_REGEX = Regex(PATTERN)

        @JvmStatic
        fun of(raw: String?): Grr {
            val normalized = raw?.trim()?.uppercase().orEmpty()
            require(GRR_REGEX.matches(normalized)) {
                "GRR inválido: $raw (esperado GRR + 8 dígitos)"
            }
            return Grr(normalized)
        }
    }
}
