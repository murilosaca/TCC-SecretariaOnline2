package br.ufpr.sept.so2.shared.domain.valueobject

data class Cpf private constructor(val value: String) {

    fun masked(): String =
        "***.${value.substring(3, 6)}.${value.substring(6, 9)}-**"

    override fun toString(): String = value

    companion object {
        @JvmStatic
        fun of(raw: String?): Cpf {
            val digits = raw?.replace(Regex("\\D"), "").orEmpty()
            require(digits.length == 11 && isValid(digits)) {
                "CPF inválido: $raw"
            }
            return Cpf(digits)
        }

        private fun isValid(digits: String): Boolean {
            if (digits.toSet().size == 1) {
                return false
            }
            val d1 = checksum(digits, 10)
            val d2 = checksum(digits, 11)
            return d1 == digits[9].digitToInt() && d2 == digits[10].digitToInt()
        }

        private fun checksum(digits: String, weightStart: Int): Int {
            var sum = 0
            for (i in 0 until weightStart - 1) {
                sum += digits[i].digitToInt() * (weightStart - i)
            }
            val result = 11 - (sum % 11)
            return if (result >= 10) 0 else result
        }
    }
}
