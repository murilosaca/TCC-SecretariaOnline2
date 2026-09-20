package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.bff.application.ports.CertificadosDashboardQueryPort
import br.ufpr.sept.so2.modules.certificados.application.ports.BeneficiarioPort
import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class CertificadosDashboardQueryAdapter(
    private val beneficiarioPort: BeneficiarioPort,
    private val certificadoRepository: CertificadoRepository,
) : CertificadosDashboardQueryPort {

    @Transactional(readOnly = true)
    override fun contarDoAluno(usuarioId: UUID): Int? {
        val aluno = beneficiarioPort.resolverPorUsuario(usuarioId) ?: return null
        return certificadoRepository.countByAluno(aluno.id).toInt()
    }
}
