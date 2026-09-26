package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.bff.application.ports.TccsProfessorDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.TccsProfessorDashboardQueryPort.ItemResumo
import br.ufpr.sept.so2.modules.bff.application.ports.TccsProfessorDashboardQueryPort.TccsProfessorDashboard
import br.ufpr.sept.so2.modules.tcc.application.ListarFilaRevisaoTccUseCase
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class TccsProfessorDashboardQueryAdapter(
    private val listarFilaRevisaoTccUseCase: ListarFilaRevisaoTccUseCase,
) : TccsProfessorDashboardQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(revisorId: UUID): TccsProfessorDashboard {
        val page = listarFilaRevisaoTccUseCase.execute(
            revisorId,
            null,
            PageRequest.of(0, FILA_LIMITE),
        )
        return TccsProfessorDashboard(
            page.totalElements.toInt(),
            page.content.map(::toItem),
        )
    }

    companion object {
        private const val FILA_LIMITE = 5

        private fun toItem(tcc: Tcc): ItemResumo =
            ItemResumo(
                tcc.id,
                tcc.titulo,
                tcc.estado.name,
                "/tccs/${tcc.id}",
            )
    }
}
