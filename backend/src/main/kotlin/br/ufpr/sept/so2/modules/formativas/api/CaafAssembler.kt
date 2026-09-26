package br.ufpr.sept.so2.modules.formativas.api

import br.ufpr.sept.so2.modules.formativas.api.dto.CaafFormativaResponse
import br.ufpr.sept.so2.modules.formativas.api.dto.CaafPoolResponse
import br.ufpr.sept.so2.modules.formativas.application.CaafAcesso
import br.ufpr.sept.so2.modules.formativas.application.CaafPoolVisao
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoResumoPort
import br.ufpr.sept.so2.modules.formativas.application.ports.AutorFormativaPort
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CaafAssembler(
    private val alunoResumoPort: AlunoResumoPort,
    private val autorFormativaPort: AutorFormativaPort,
) {
    fun from(visao: CaafPoolVisao): CaafPoolResponse {
        val collection = linkedMapOf("self" to SELF)
        if (visao.membros.any { it.id == visao.meuId }) {
            collection[CaafAcesso.ATRIBUIR] = ATRIBUICOES
            if (visao.itens.any { it.elegivelAprovacaoEmLote() }) {
                collection[CaafAcesso.BATCH_APPROVE] = LOTE
            }
        }
        return CaafPoolResponse(
            visao.meuId,
            visao.kpis,
            visao.membros,
            visao.itens.map { item(it, visao.meuId) },
            collection,
        )
    }

    private fun item(formativa: Formativa, usuarioId: UUID): CaafFormativaResponse {
        val links = linkedMapOf("self" to "/formativas/${formativa.id}")
        if (formativa.podeAtribuir(usuarioId)) {
            links[CaafAcesso.ATRIBUIR] = ATRIBUICOES
        }
        if (formativa.elegivelAprovacaoEmLote()) {
            links[CaafAcesso.BATCH_APPROVE] = LOTE
        }
        if (formativa.estado.podeRevisar()) {
            links["revisar"] = "/formativas/${formativa.id}/revisar"
        }
        return CaafFormativaResponse(
            formativa.id,
            formativa.idAluno,
            alunoResumoPort.nomeDe(formativa.idAluno),
            alunoResumoPort.cursoDe(formativa.idAluno),
            formativa.origem.name,
            formativa.titulo,
            formativa.cargaHoraria,
            formativa.estado.name,
            formativa.createdAt,
            formativa.idResponsavel,
            formativa.idResponsavel?.let { autorFormativaPort.rotulo(it) },
            formativa.semResponsavel(),
            formativa.atribuidaA(usuarioId),
            formativa.elegivelAprovacaoEmLote(),
            links,
        )
    }

    companion object {
        const val SELF = "/comissoes/caaf"
        const val ATRIBUICOES = "/comissoes/caaf/atribuicoes"
        const val LOTE = "/comissoes/caaf/lote"
    }
}
