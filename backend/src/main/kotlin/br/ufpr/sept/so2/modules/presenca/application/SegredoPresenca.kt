package br.ufpr.sept.so2.modules.presenca.application

import java.security.SecureRandom
import java.util.Base64

object SegredoPresenca {
    private val RANDOM = SecureRandom()

    fun pin(): String = String.format("%06d", RANDOM.nextInt(1_000_000))

    fun qrToken(): String {
        val bytes = ByteArray(24)
        RANDOM.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
