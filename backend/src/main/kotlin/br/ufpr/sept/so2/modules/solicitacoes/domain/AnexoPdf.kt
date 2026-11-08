package br.ufpr.sept.so2.modules.solicitacoes.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.security.MessageDigest

/**
 * PDF enviado no wizard. Não é certificado: não há assinatura nem emissão oficial.
 */
object AnexoPdf {
    const val LIMITE_BYTES: Int = 10 * 1024 * 1024
    const val MSG_TAMANHO: String = "O arquivo excede o tamanho máximo de 10 MB."
    const val MSG_TIPO: String = "O anexo deve ser um PDF."
    const val MSG_HASH: String = "O hash do anexo não confere."

    fun validar(nome: String, contentType: String, bytes: ByteArray, sha256Informado: String?): String {
        if (bytes.size > LIMITE_BYTES) {
            throw DadoInvalidoException(MSG_TAMANHO)
        }
        val tipo = contentType.substringBefore(';').trim().lowercase()
        val parecePdf = tipo == "application/pdf" || nome.trim().lowercase().endsWith(".pdf")
        val cabecalho = bytes.size >= 5 && bytes.copyOfRange(0, 5).contentEquals(CABECALHO)
        if (!parecePdf || !cabecalho) {
            throw DadoInvalidoException(MSG_TIPO)
        }
        val hash = sha256(bytes)
        if (!sha256Informado.isNullOrBlank() && !sha256Informado.equals(hash, ignoreCase = true)) {
            throw DadoInvalidoException(MSG_HASH)
        }
        return hash
    }

    fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private val CABECALHO: ByteArray = "%PDF-".toByteArray(Charsets.US_ASCII)
}
