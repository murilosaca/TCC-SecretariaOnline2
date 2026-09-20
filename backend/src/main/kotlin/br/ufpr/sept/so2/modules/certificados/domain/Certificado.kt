package br.ufpr.sept.so2.modules.certificados.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.OffsetDateTime
import java.util.UUID

class Certificado(
    val id: UUID,
    val idAluno: UUID,
    val idFormativa: UUID?,
    val idEvento: UUID?,
    val tipo: CertificadoTipo,
    val titulo: String,
    val cargaHoraria: Int,
    val beneficiarioNome: String,
    val hashSha256: String,
    val assinatura: String,
    val pdf: ByteArray,
    val emitidoEm: OffsetDateTime,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    init {
        validar(titulo, cargaHoraria, beneficiarioNome, hashSha256, assinatura, pdf, tipo, idFormativa)
    }

    fun pertenceAoAluno(alunoId: UUID): Boolean = idAluno == alunoId

    companion object {
        val HASH_SHA256 = Regex("^[0-9a-f]{64}$")

        fun deFormativa(
            id: UUID,
            idAluno: UUID,
            idFormativa: UUID,
            idEvento: UUID?,
            titulo: String,
            cargaHoraria: Int,
            beneficiarioNome: String,
            hashSha256: String,
            assinatura: String,
            pdf: ByteArray,
            agora: OffsetDateTime,
        ): Certificado = Certificado(
            id,
            idAluno,
            idFormativa,
            idEvento,
            CertificadoTipo.FORMATIVA,
            titulo,
            cargaHoraria,
            beneficiarioNome,
            hashSha256,
            assinatura,
            pdf,
            agora,
            agora,
            agora,
        )

        private fun validar(
            titulo: String?,
            cargaHoraria: Int,
            beneficiarioNome: String?,
            hashSha256: String?,
            assinatura: String?,
            pdf: ByteArray?,
            tipo: CertificadoTipo,
            idFormativa: UUID?,
        ) {
            if (titulo.isNullOrBlank()) {
                throw DadoInvalidoException("Título do certificado é obrigatório.")
            }
            if (cargaHoraria <= 0) {
                throw DadoInvalidoException("Carga horária do certificado deve ser positiva.")
            }
            if (beneficiarioNome.isNullOrBlank()) {
                throw DadoInvalidoException("Nome do beneficiário é obrigatório.")
            }
            val hash = hashSha256?.trim()?.lowercase().orEmpty()
            if (!HASH_SHA256.matches(hash)) {
                throw DadoInvalidoException("Hash SHA-256 do certificado é inválido.")
            }
            if (assinatura.isNullOrBlank()) {
                throw DadoInvalidoException("Assinatura do certificado é obrigatória.")
            }
            if (pdf == null || pdf.isEmpty()) {
                throw DadoInvalidoException("PDF do certificado é obrigatório.")
            }
            if (tipo == CertificadoTipo.FORMATIVA && idFormativa == null) {
                throw DadoInvalidoException("Certificado de formativa exige id da formativa.")
            }
        }
    }
}
