package br.ufpr.sept.so2.modules.tcc.api

import br.ufpr.sept.so2.modules.tcc.api.dto.AvaliacaoTccResponse
import br.ufpr.sept.so2.modules.tcc.api.dto.MembroBancaResponse
import br.ufpr.sept.so2.modules.tcc.api.dto.TccResponse
import br.ufpr.sept.so2.modules.tcc.application.TccAcesso
import br.ufpr.sept.so2.modules.tcc.application.ports.AlunoTccPort
import br.ufpr.sept.so2.modules.tcc.application.ports.AutorTccPort
import br.ufpr.sept.so2.modules.tcc.domain.PapelBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TccAssembler(
    private val alunoTccPort: AlunoTccPort,
    private val autorTccPort: AutorTccPort,
) {
    fun from(tcc: Tcc, usuarioId: UUID, alunoId: UUID?, authorities: List<String>): TccResponse {
        val dono = alunoId != null && tcc.pertenceAoAluno(alunoId)
        val membro = if (TccAcesso.REVIEW in authorities) tcc.membroDe(usuarioId) else null
        val self = "/tccs/${tcc.id}"
        val links = linkedMapOf("self" to self)
        if (dono && tcc.podeEnviar()) {
            links["upload-final"] = "$self/versao-final"
        }
        if (membro != null && tcc.podeAvaliar(usuarioId)) {
            links["avaliar"] = "$self/avaliacoes"
        }
        if ((dono || membro != null) && tcc.temArquivo()) {
            links["download"] = "$self/arquivo"
        }
        val orientador = tcc.membros.first { it.papel == PapelBancaTcc.ORIENTADOR }
        return TccResponse(
            tcc.id,
            tcc.idAluno,
            alunoTccPort.nomeDe(tcc.idAluno),
            tcc.idCurso,
            tcc.titulo,
            tcc.situacao.name,
            tcc.estado.name,
            tcc.dataDefesa,
            tcc.dataEntrega,
            autorTccPort.rotulo(orientador.idUsuario),
            membro?.papel?.name,
            tcc.nomeArquivo,
            tcc.tamanho,
            tcc.enviadoEm,
            tcc.membros.map { item ->
                MembroBancaResponse(item.id, item.idUsuario, autorTccPort.rotulo(item.idUsuario), item.papel.name)
            },
            tcc.avaliacoes.map { item ->
                AvaliacaoTccResponse(
                    item.id,
                    item.idAutor,
                    autorTccPort.rotulo(item.idAutor),
                    item.papel.name,
                    item.acao.name,
                    item.resultado.name,
                    item.nota,
                    item.parecer,
                    item.createdAt,
                )
            },
            links,
        )
    }
}
