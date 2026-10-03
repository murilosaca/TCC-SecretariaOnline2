package br.ufpr.sept.so2.modules.atendimentos.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

/**
 * Anexo opcional do atendimento: só PDF, até 10 MB (CA-F5-007-04).
 * O upload é server-side pela `ObjectStoragePort`; não existe PUT pré-assinado aqui.
 */
object AnexoAtendimento {
    const val MAX_BYTES = 10 * 1024 * 1024
    const val MENSAGEM = "Envie um PDF de até 10 MB."
    const val CONTENT_TYPE = "application/pdf"

    fun validar(bytes: ByteArray) {
        if (bytes.isEmpty() || bytes.size > MAX_BYTES) {
            throw DadoInvalidoException(MENSAGEM)
        }
        if (!ePdf(bytes)) {
            throw DadoInvalidoException(MENSAGEM)
        }
    }

    private fun ePdf(bytes: ByteArray): Boolean = bytes.size >= 4 &&
        bytes[0] == '%'.code.toByte() &&
        bytes[1] == 'P'.code.toByte() &&
        bytes[2] == 'D'.code.toByte() &&
        bytes[3] == 'F'.code.toByte()
}
