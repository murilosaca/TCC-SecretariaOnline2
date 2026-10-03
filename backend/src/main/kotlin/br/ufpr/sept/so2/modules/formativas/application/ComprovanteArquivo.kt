package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

object ComprovanteArquivo {
    const val MAX_BYTES = 5 * 1024 * 1024
    const val MENSAGEM = "Envie um PDF, JPEG ou PNG de até 5 MB."

    fun extensao(bytes: ByteArray): String {
        if (bytes.isEmpty() || bytes.size > MAX_BYTES) {
            throw DadoInvalidoException(MENSAGEM)
        }
        if (bytes.size >= 4 && bytes[0] == '%'.code.toByte() && bytes[1] == 'P'.code.toByte() &&
            bytes[2] == 'D'.code.toByte() && bytes[3] == 'F'.code.toByte()
        ) {
            return "pdf"
        }
        if (bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
            return "jpg"
        }
        if (bytes.size >= 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
        ) {
            return "png"
        }
        throw DadoInvalidoException(MENSAGEM)
    }

    fun contentType(extensao: String): String = when (extensao) {
        "pdf" -> "application/pdf"
        "jpg" -> "image/jpeg"
        else -> "image/png"
    }
}
