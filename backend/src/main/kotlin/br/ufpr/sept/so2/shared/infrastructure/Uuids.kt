package br.ufpr.sept.so2.shared.infrastructure

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.UUID

/**
 * Gerador de UUID v7 (timestamp + aleatório) para ordenação cronológica
 * e mitigação de enumeração, conforme a especificação do SO2.
 */
object Uuids {
    private val RANDOM = SecureRandom()

    @JvmStatic
    fun v7(): UUID {
        val bytes = ByteArray(16)
        RANDOM.nextBytes(bytes)

        val timestampMs = System.currentTimeMillis()
        bytes[0] = ((timestampMs ushr 40) and 0xFF).toByte()
        bytes[1] = ((timestampMs ushr 32) and 0xFF).toByte()
        bytes[2] = ((timestampMs ushr 24) and 0xFF).toByte()
        bytes[3] = ((timestampMs ushr 16) and 0xFF).toByte()
        bytes[4] = ((timestampMs ushr 8) and 0xFF).toByte()
        bytes[5] = (timestampMs and 0xFF).toByte()

        bytes[6] = ((bytes[6].toInt() and 0x0F) or 0x70).toByte()
        bytes[8] = ((bytes[8].toInt() and 0x3F) or 0x80).toByte()

        val buffer = ByteBuffer.wrap(bytes)
        return UUID(buffer.long, buffer.long)
    }
}
