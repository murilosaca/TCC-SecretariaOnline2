package br.ufpr.sept.so2.modules.egresso.application.ports

import br.ufpr.sept.so2.modules.egresso.application.CertificadoDoEgresso
import br.ufpr.sept.so2.modules.egresso.application.PdfDoEgresso
import java.util.UUID

interface EgressoConsultaPort {
    fun consultar(usuarioId: UUID): Cadastro?

    fun certificado(certificadoId: UUID, alunoId: UUID): PdfDoEgresso?

    data class Cadastro(
        val alunoId: UUID,
        val egresso: Boolean,
        val nome: String,
        val curso: String?,
        val horasFormativasValidadas: Int,
        val totalCertificados: Int,
        val certificados: List<CertificadoDoEgresso>,
    )
}
