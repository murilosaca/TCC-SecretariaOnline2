package br.ufpr.sept.so2.modules.certificados.application.ports

import java.time.OffsetDateTime

fun interface PdfCertificadoRenderer {
    fun render(dados: Dados, qrUrl: String?): ByteArray

    data class Dados(
        val beneficiarioNome: String,
        val titulo: String,
        val cargaHoraria: Int,
        val emitidoEm: OffsetDateTime,
        val hashSha256: String?,
    )
}
