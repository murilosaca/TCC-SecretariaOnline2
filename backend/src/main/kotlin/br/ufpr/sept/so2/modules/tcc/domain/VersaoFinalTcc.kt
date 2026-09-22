package br.ufpr.sept.so2.modules.tcc.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

object VersaoFinalTcc {
    const val PDF_MAX_BYTES = 5 * 1024 * 1024
    private val PDF_MAGIC = byteArrayOf(0x25, 0x50, 0x44, 0x46)

    fun validar(nome: String, contentType: String?, bytes: ByteArray): String {
        if (bytes.isEmpty() || bytes.size > PDF_MAX_BYTES) {
            throw DadoInvalidoException("Envie um PDF de até 5 MB.")
        }
        val cabecalhoPdf = bytes.size >= PDF_MAGIC.size &&
            PDF_MAGIC.indices.all { bytes[it] == PDF_MAGIC[it] }
        val tipo = contentType?.substringBefore(';')?.trim()?.lowercase()
        val tipoAceito = tipo == null ||
            tipo == "application/pdf" ||
            tipo == "application/x-pdf" ||
            tipo == "application/octet-stream"
        if (!cabecalhoPdf || !tipoAceito) {
            throw DadoInvalidoException("O arquivo precisa ser um PDF.")
        }
        val limpo = nome.trim().ifEmpty { "tcc.pdf" }
        return limpo.take(200)
    }
}
