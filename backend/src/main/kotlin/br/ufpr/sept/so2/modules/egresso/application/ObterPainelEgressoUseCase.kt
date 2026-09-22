package br.ufpr.sept.so2.modules.egresso.application

import br.ufpr.sept.so2.modules.egresso.application.ports.EgressoConsultaPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ObterPainelEgressoUseCase(
    private val consulta: EgressoConsultaPort,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID, authorities: Collection<String>): PainelEgresso {
        EgressoAcesso.exigirSemAlunoAtivo(authorities)
        val cadastro = EgressoAcesso.exigirEgresso(consulta.consultar(usuarioId))
        return PainelEgresso(
            cadastro.nome,
            cadastro.curso,
            cadastro.horasFormativasValidadas,
            cadastro.totalCertificados,
            cadastro.certificados,
        )
    }
}
