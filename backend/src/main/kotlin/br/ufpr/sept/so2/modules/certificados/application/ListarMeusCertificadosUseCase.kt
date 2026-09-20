package br.ufpr.sept.so2.modules.certificados.application

import br.ufpr.sept.so2.modules.certificados.application.ports.BeneficiarioPort
import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoRepository
import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListarMeusCertificadosUseCase(
    private val beneficiarioPort: BeneficiarioPort,
    private val certificadoRepository: CertificadoRepository,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID, beneficiario: String, pageable: Pageable): Page<Certificado> {
        if (!beneficiario.equals("me", ignoreCase = true)) {
            throw AcessoNegadoException("Neste momento só é possível listar os próprios certificados.")
        }
        val aluno = CertificadoAcesso.exigirAlunoAtivo(beneficiarioPort, usuarioId)
        val size = pageable.pageSize.coerceIn(1, 100)
        val sort = if (pageable.sort.isSorted) pageable.sort else Sort.by(Sort.Direction.DESC, "emitidoEm")
        return certificadoRepository.findByAluno(aluno.id, PageRequest.of(pageable.pageNumber, size, sort))
    }
}
