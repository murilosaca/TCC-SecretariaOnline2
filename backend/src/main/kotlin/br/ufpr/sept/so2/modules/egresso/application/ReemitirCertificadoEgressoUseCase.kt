package br.ufpr.sept.so2.modules.egresso.application

import br.ufpr.sept.so2.modules.egresso.application.ports.EgressoConsultaPort
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Devolve o PDF já gravado. Não insere certificado, não recalcula hash e não assina de novo.
 */
@Service
class ReemitirCertificadoEgressoUseCase(
    private val consulta: EgressoConsultaPort,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID, authorities: Collection<String>, certificadoId: UUID): PdfDoEgresso {
        EgressoAcesso.exigirSemAlunoAtivo(authorities)
        val cadastro = EgressoAcesso.exigirEgresso(consulta.consultar(usuarioId))
        return consulta.certificado(certificadoId, cadastro.alunoId)
            ?: throw RecursoNaoEncontradoException("Certificado não encontrado.")
    }
}
