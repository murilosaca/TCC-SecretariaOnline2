package br.ufpr.sept.so2.modules.egresso.application

import br.ufpr.sept.so2.modules.egresso.application.ColacaoDoEgresso
import br.ufpr.sept.so2.modules.egresso.application.PainelEgresso
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
        val diploma = cadastro.diploma
        return PainelEgresso(
            cadastro.nome,
            cadastro.curso,
            diploma?.dataColacao,
            cadastro.horasFormativasValidadas,
            cadastro.totalCertificados,
            diploma?.let { "EMITIDO" },
            diploma,
            diploma?.let { ColacaoDoEgresso(it.dataColacao, it.turma) },
            cadastro.certificados,
        )
    }
}
