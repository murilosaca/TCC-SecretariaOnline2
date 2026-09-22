package br.ufpr.sept.so2.modules.estagio.api

import br.ufpr.sept.so2.modules.estagio.api.dto.CoeEstagioResponse
import br.ufpr.sept.so2.modules.estagio.api.dto.CoePoolResponse
import br.ufpr.sept.so2.modules.estagio.application.CoeAcesso
import br.ufpr.sept.so2.modules.estagio.application.CoePoolVisao
import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort
import br.ufpr.sept.so2.modules.estagio.application.ports.AutorEstagioPort
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CoeAssembler(
    private val alunoEstagioPort: AlunoEstagioPort,
    private val autorEstagioPort: AutorEstagioPort,
) {
    fun from(visao: CoePoolVisao): CoePoolResponse {
        val collection = linkedMapOf("self" to SELF)
        if (visao.membros.any { it.id == visao.meuId }) {
            collection[CoeAcesso.ATRIBUIR] = ATRIBUICOES
        }
        return CoePoolResponse(
            visao.meuId,
            visao.kpis,
            visao.membros,
            visao.itens.map { item(it, visao.meuId) },
            collection,
        )
    }

    private fun item(estagio: Estagio, usuarioId: UUID): CoeEstagioResponse {
        val links = linkedMapOf("self" to "/estagios/${estagio.id}")
        if (estagio.podeAtribuir(usuarioId)) {
            links[CoeAcesso.ATRIBUIR] = ATRIBUICOES
        }
        return CoeEstagioResponse(
            estagio.id,
            estagio.idAluno,
            alunoEstagioPort.nomeDe(estagio.idAluno),
            estagio.idCurso,
            estagio.idOrientador,
            estagio.idOrientador?.let { autorEstagioPort.rotulo(it) },
            estagio.empresa,
            estagio.inicio,
            estagio.documentoPendente(),
            estagio.semOrientador(),
            estagio.orientadoPor(usuarioId),
            links,
        )
    }

    companion object {
        const val SELF = "/comissoes/coe"
        const val ATRIBUICOES = "/comissoes/coe/atribuicoes"
    }
}
