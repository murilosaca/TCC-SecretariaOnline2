package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.bff.application.ports.FormativasDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.FormativasDashboardQueryPort.FormativasDashboard
import br.ufpr.sept.so2.modules.bff.application.ports.FormativasDashboardQueryPort.PendenciaFormativa
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class FormativasDashboardQueryAdapter(
    private val alunoPorUsuarioPort: AlunoPorUsuarioPort,
    private val formativaRepository: FormativaRepository,
) : FormativasDashboardQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(usuarioId: UUID): FormativasDashboard {
        val aluno = alunoPorUsuarioPort.resolver(usuarioId)
            ?: return FormativasDashboard(0, 0, emptyList())
        val validadas = formativaRepository.somarCargaHoraria(aluno.id, FormativaEstado.APROVADA)
        val pendentes = formativaRepository.findPendentesConfirmacao(aluno.id, 3).map { item ->
            PendenciaFormativa(
                item.id,
                item.titulo,
                item.estado.name,
                "/formativas/${item.id}",
            )
        }
        return FormativasDashboard(validadas, aluno.horasFormativasMinimas, pendentes)
    }
}
