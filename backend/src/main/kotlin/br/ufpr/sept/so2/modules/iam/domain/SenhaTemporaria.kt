package br.ufpr.sept.so2.modules.iam.domain

import java.security.SecureRandom

/**
 * Senha forte gerada no servidor. Nunca retornada à API/UI do admin (RF-F7-001).
 */
object SenhaTemporaria {
    private val RANDOM = SecureRandom()
    private const val MAIUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val MINUSCULAS = "abcdefghijkmnopqrstuvwxyz"
    private const val DIGITOS = "23456789"
    private const val ESPECIAIS = "!@#$%&*"
    private const val TODOS = MAIUSCULAS + MINUSCULAS + DIGITOS + ESPECIAIS

    fun gerar(tamanho: Int = 16): String {
        require(tamanho >= PoliticaSenha.TAMANHO_MINIMO)
        val chars = CharArray(tamanho)
        chars[0] = MAIUSCULAS[RANDOM.nextInt(MAIUSCULAS.length)]
        chars[1] = MINUSCULAS[RANDOM.nextInt(MINUSCULAS.length)]
        chars[2] = DIGITOS[RANDOM.nextInt(DIGITOS.length)]
        chars[3] = ESPECIAIS[RANDOM.nextInt(ESPECIAIS.length)]
        for (i in 4 until tamanho) {
            chars[i] = TODOS[RANDOM.nextInt(TODOS.length)]
        }
        // Fisher–Yates
        for (i in chars.indices.reversed()) {
            val j = RANDOM.nextInt(i + 1)
            val tmp = chars[i]
            chars[i] = chars[j]
            chars[j] = tmp
        }
        return String(chars)
    }
}
