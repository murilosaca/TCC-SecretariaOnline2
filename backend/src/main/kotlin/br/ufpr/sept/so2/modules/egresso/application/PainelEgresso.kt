package br.ufpr.sept.so2.modules.egresso.application

import java.time.OffsetDateTime
import java.util.UUID

data class CertificadoDoEgresso(
    val id: UUID,
    val titulo: String,
    val tipo: String,
    val emitidoEm: OffsetDateTime,
    val hashSha256: String,
)

data class PainelEgresso(
    val nome: String,
    val curso: String?,
    val horasFormativasValidadas: Int,
    val totalCertificados: Int,
    val certificados: List<CertificadoDoEgresso>,
)

class PdfDoEgresso(
    val id: UUID,
    val titulo: String,
    val storageKey: String,
    val hashSha256: String,
)
