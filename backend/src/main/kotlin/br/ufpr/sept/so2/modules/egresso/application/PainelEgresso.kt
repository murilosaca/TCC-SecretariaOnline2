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

data class DiplomaDoEgresso(
    val id: UUID,
    val numero: String,
    val emitidoEm: OffsetDateTime,
    val situacao: String,
    val dataColacao: OffsetDateTime,
    val turma: String?,
    val storageKey: String?,
)

data class PainelEgresso(
    val nome: String,
    val curso: String?,
    val concluidoEm: OffsetDateTime?,
    val horasFormativasValidadas: Int,
    val totalCertificados: Int,
    val situacaoDiploma: String?,
    val diploma: DiplomaDoEgresso?,
    val colacao: ColacaoDoEgresso?,
    val certificados: List<CertificadoDoEgresso>,
)

data class ColacaoDoEgresso(
    val data: OffsetDateTime?,
    val turma: String?,
)

class PdfDoEgresso(
    val id: UUID,
    val titulo: String,
    val storageKey: String,
    val hashSha256: String,
)
