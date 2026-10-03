package br.ufpr.sept.so2.modules.atendimentos.api

import br.ufpr.sept.so2.modules.atendimentos.api.dto.AtendimentoResponse
import br.ufpr.sept.so2.modules.atendimentos.api.dto.CategoriaAtendimentoResponse
import br.ufpr.sept.so2.modules.atendimentos.api.dto.CategoriasAtendimentoResponse
import br.ufpr.sept.so2.modules.atendimentos.application.AtendimentoItem
import br.ufpr.sept.so2.modules.atendimentos.domain.CategoriaAtendimento
import org.springframework.stereotype.Component

@Component
class AtendimentoAssembler {

    /**
     * `acknowledge` é o único gatilho de ação do aluno e só existe enquanto a
     * ciência está pendente (RN-F1.20-02). O aluno nunca recebe link de criar
     * nem de contestar.
     */
    fun from(item: AtendimentoItem, authorities: List<String>): AtendimentoResponse {
        val atendimento = item.atendimento
        val links = linkedMapOf<String, String>()
        if (VIEW_OWN in authorities) {
            if (atendimento.pendenteDeCiencia()) {
                links["acknowledge"] = "/atendimentos/${atendimento.id}/acknowledge"
            }
            if (atendimento.temAnexo()) {
                links["anexo"] = "/atendimentos/${atendimento.id}/anexo"
            }
        }
        return AtendimentoResponse(
            atendimento.id,
            atendimento.idAluno,
            atendimento.idCategoria,
            item.categoriaNome,
            atendimento.assunto,
            atendimento.resposta,
            atendimento.estado.name,
            atendimento.createdAt,
            atendimento.cienciaEm,
            atendimento.temAnexo(),
            links,
        )
    }

    fun categorias(categorias: List<CategoriaAtendimento>, authorities: List<String>): CategoriasAtendimentoResponse {
        val links = linkedMapOf<String, String>()
        if (CREATE in authorities) {
            links["registrar"] = "/atendimentos"
        }
        return CategoriasAtendimentoResponse(
            categorias.map { CategoriaAtendimentoResponse(it.id, it.nome) },
            links,
        )
    }

    companion object {
        const val CREATE = "service_record.create"
        const val VIEW_OWN = "service_record.view_own"
    }
}
