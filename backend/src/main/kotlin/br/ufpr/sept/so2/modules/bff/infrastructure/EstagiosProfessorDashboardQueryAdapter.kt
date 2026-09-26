package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.bff.application.ports.EstagiosProfessorDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.EstagiosProfessorDashboardQueryPort.EstagiosProfessorDashboard
import br.ufpr.sept.so2.modules.bff.application.ports.EstagiosProfessorDashboardQueryPort.ItemResumo
import br.ufpr.sept.so2.modules.estagio.application.ListarFilaRevisaoEstagioUseCase
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class EstagiosProfessorDashboardQueryAdapter(
    private val listarFilaRevisaoEstagioUseCase: ListarFilaRevisaoEstagioUseCase,
) : EstagiosProfessorDashboardQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(orientadorId: UUID): EstagiosProfessorDashboard {
        val page = listarFilaRevisaoEstagioUseCase.execute(
            orientadorId,
            null,
            PageRequest.of(0, FILA_LIMITE),
        )
        return EstagiosProfessorDashboard(
            page.totalElements.toInt(),
            page.content.map(::toItem),
        )
    }

    companion object {
        private const val FILA_LIMITE = 5

        private fun toItem(estagio: Estagio): ItemResumo =
            ItemResumo(
                estagio.id,
                estagio.empresa,
                estagio.situacao.name,
                "/estagios/${estagio.id}",
            )
    }
}
