package br.ufpr.sept.so2.modules.iam.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

object FotoPerfilRegras {
    const val MAX_BYTES: Int = 2 * 1024 * 1024

    data class Imagem(val contentType: String, val extensao: String)

    fun validar(bytes: ByteArray): Imagem {
        if (bytes.isEmpty()) {
            throw DadoInvalidoException("A imagem é obrigatória.")
        }
        if (bytes.size > MAX_BYTES) {
            throw DadoInvalidoException("A imagem deve ter no máximo 2 MB.")
        }
        return detectar(bytes) ?: throw DadoInvalidoException("Use JPEG, PNG ou WebP.")
    }

    fun detectar(bytes: ByteArray): Imagem? {
        if (bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()
        ) {
            return Imagem("image/jpeg", "jpg")
        }
        if (bytes.size >= 8 &&
            bytes[0] == 0x89.toByte() &&
            bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() &&
            bytes[3] == 0x47.toByte()
        ) {
            return Imagem("image/png", "png")
        }
        if (bytes.size >= 12 &&
            bytes[0] == 'R'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() &&
            bytes[11] == 'P'.code.toByte()
        ) {
            return Imagem("image/webp", "webp")
        }
        return null
    }
}
