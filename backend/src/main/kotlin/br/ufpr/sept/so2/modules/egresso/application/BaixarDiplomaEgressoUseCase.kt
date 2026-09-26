package br.ufpr.sept.so2.modules.egresso.application

import br.ufpr.sept.so2.modules.egresso.application.ports.EgressoConsultaPort
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BaixarDiplomaEgressoUseCase(
    private val consulta: EgressoConsultaPort,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID, authorities: Collection<String>): DiplomaDoEgresso {
        EgressoAcesso.exigirSemAlunoAtivo(authorities)
        val cadastro = EgressoAcesso.exigirEgresso(consulta.consultar(usuarioId))
        val diploma = consulta.diploma(cadastro.alunoId)
            ?: throw RecursoNaoEncontradoException("Diploma não encontrado.")
        if (diploma.storageKey.isNullOrBlank()) {
            throw RecursoNaoEncontradoException("PDF do diploma ainda não foi registrado.")
        }
        return diploma
    }
}
