package br.ufpr.sept.so2.modules.presenca.application

import java.security.SecureRandom

object PinPresenca {
    private val RANDOM = SecureRandom()

    fun gerar(): String = String.format("%06d", RANDOM.nextInt(1_000_000))
}
