package br.ufpr.sept.so2.modules.formativas.api

import br.ufpr.sept.so2.modules.formativas.api.dto.FormativaResponse
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import org.springframework.stereotype.Component
import java.util.LinkedHashMap
import java.util.UUID

@Component
class FormativaAssembler {

    fun from(
        formativa: Formativa,
        alunoId: UUID,
        authorities: List<String>,
    ): FormativaResponse {
        val self = "/formativas/${formativa.id}"
        val links = LinkedHashMap<String, String>()
        links["self"] = self
        val dono = formativa.idAluno == alunoId
        val pendente = formativa.estado == FormativaEstado.PENDENTE_CONFIRMACAO
        if (pendente && dono && AUTHORITY_CONFIRM in authorities) {
            links["confirmar"] = "$self/confirmar"
        }
        if (pendente && dono) {
            links["cancelar"] = "$self/cancelar"
        }
        return FormativaResponse(
            formativa.id,
            formativa.idAluno,
            formativa.idEvento,
            formativa.origem.name,
            formativa.titulo,
            formativa.cargaHoraria,
            formativa.estado.name,
            formativa.createdAt,
            formativa.updatedAt,
            links,
        )
    }

    companion object {
        const val AUTHORITY_VIEW = "formative.view_own"
        const val AUTHORITY_CONFIRM = "formative.confirm_own"
    }
}
