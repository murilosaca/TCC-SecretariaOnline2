package br.ufpr.sept.so2.modules.formativas.api

import br.ufpr.sept.so2.modules.formativas.api.dto.FormativaResponse
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoResumoPort
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import org.springframework.stereotype.Component
import java.util.LinkedHashMap
import java.util.UUID

@Component
class FormativaAssembler(
    private val alunoResumoPort: AlunoResumoPort,
) {

    fun from(
        formativa: Formativa,
        alunoId: UUID?,
        authorities: List<String>,
    ): FormativaResponse {
        val self = "/formativas/${formativa.id}"
        val links = LinkedHashMap<String, String>()
        links["self"] = self
        val dono = alunoId != null && formativa.idAluno == alunoId
        val pendente = formativa.estado == FormativaEstado.PENDENTE_CONFIRMACAO
        if (pendente && dono && AUTHORITY_CONFIRM in authorities) {
            links["confirmar"] = "$self/confirmar"
        }
        if (pendente && dono) {
            links["cancelar"] = "$self/cancelar"
        }
        if (formativa.estado.podeRevisar() && AUTHORITY_REVIEW in authorities) {
            links["revisar"] = self
            links["aprovar"] = "$self/aprovar"
            links["indeferir"] = "$self/indeferir"
        }
        return FormativaResponse(
            formativa.id,
            formativa.idAluno,
            alunoResumoPort.nomeDe(formativa.idAluno),
            formativa.idEvento,
            formativa.origem.name,
            formativa.titulo,
            formativa.cargaHoraria,
            formativa.estado.name,
            formativa.parecer,
            formativa.idRevisor,
            formativa.reviewedAt,
            formativa.createdAt,
            formativa.updatedAt,
            links,
        )
    }

    companion object {
        const val AUTHORITY_VIEW = "formative.view_own"
        const val AUTHORITY_CONFIRM = "formative.confirm_own"
        const val AUTHORITY_REVIEW = "formative.review"
    }
}
