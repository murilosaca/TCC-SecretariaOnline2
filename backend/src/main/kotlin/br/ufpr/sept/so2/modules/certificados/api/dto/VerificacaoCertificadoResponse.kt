package br.ufpr.sept.so2.modules.certificados.api.dto

import java.time.OffsetDateTime

data class VerificacaoCertificadoResponse(
    val status: String,
    val hashSha256: String,
    val assinaturaEd25519: String,
    val beneficiarioNome: String,
    val atividade: String,
    val cargaHoraria: Int,
    val emitidoEm: OffsetDateTime,
    val jwksUrl: String,
)
