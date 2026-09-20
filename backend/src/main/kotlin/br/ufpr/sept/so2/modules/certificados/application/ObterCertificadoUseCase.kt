package br.ufpr.sept.so2.modules.certificados.application

import br.ufpr.sept.so2.modules.certificados.application.ports.BeneficiarioPort
import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoRepository
import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ObterCertificadoUseCase(
    private val beneficiarioPort: BeneficiarioPort,
    private val certificadoRepository: CertificadoRepository,
) {
    @Transactional(readOnly = true)
    fun execute(id: UUID, usuarioId: UUID): Certificado {
        val certificado = certificadoRepository.findById(id)
            ?: throw RecursoNaoEncontradoException("Certificado não encontrado.")
        val aluno = CertificadoAcesso.exigirAlunoAtivo(beneficiarioPort, usuarioId)
        CertificadoAcesso.exigirDono(certificado, aluno.id)
        return certificado
    }
}
