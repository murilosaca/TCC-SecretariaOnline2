package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.bff.application.ports.FormativasCaafDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.FormativasCaafDashboardQueryPort.FormativasCaafDashboard
import br.ufpr.sept.so2.modules.bff.application.ports.FormativasCaafDashboardQueryPort.ItemResumo
import br.ufpr.sept.so2.modules.formativas.application.ListarFilaRevisaoFormativaUseCase
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class FormativasCaafDashboardQueryAdapter(
    private val listarFilaRevisaoFormativaUseCase: ListarFilaRevisaoFormativaUseCase,
) : FormativasCaafDashboardQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(): FormativasCaafDashboard {
        val page = listarFilaRevisaoFormativaUseCase.execute(
            PageRequest.of(0, FILA_LIMITE, Sort.by(Sort.Direction.ASC, "createdAt")),
        )
        return FormativasCaafDashboard(
            page.totalElements.toInt(),
            page.content.map(::toItem),
        )
    }

    companion object {
        private const val FILA_LIMITE = 5

        private fun toItem(formativa: Formativa): ItemResumo =
            ItemResumo(
                formativa.id,
                formativa.titulo,
                formativa.estado.name,
                "/formativas/${formativa.id}",
            )
    }
}
