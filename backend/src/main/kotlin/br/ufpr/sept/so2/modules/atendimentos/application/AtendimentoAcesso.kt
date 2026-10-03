package br.ufpr.sept.so2.modules.atendimentos.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.atendimentos.application.ports.AlunoAtendimentoPort
import br.ufpr.sept.so2.modules.atendimentos.application.ports.AlunoAtendimentoPort.Cadastro
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import java.util.UUID

internal object AtendimentoAcesso {
    const val CREATE = "service_record.create"
    const val VIEW_OWN = "service_record.view_own"
    const val ALUNO_NAO_ENCONTRADO = "Aluno não encontrado."
    const val NAO_ENCONTRADO = "Atendimento não encontrado."
    const val SEM_CADASTRO = "Cadastro acadêmico de aluno não encontrado."

    fun cursos(port: CursoEscopoPort, atorId: UUID): Set<UUID> = port.cursoIdsDoUsuario(atorId)

    /** Aluno fora do escopo e aluno inexistente devolvem a mesma resposta (404). */
    fun exigirAlunoNoEscopo(cursos: Set<UUID>, port: AlunoAtendimentoPort, alunoId: UUID?): Cadastro {
        val aluno = alunoId?.let { port.cadastro(it) }
        if (aluno == null || aluno.idCurso !in cursos) {
            throw RecursoNaoEncontradoException(ALUNO_NAO_ENCONTRADO)
        }
        return aluno
    }

    fun exigirAlunoAutenticado(port: AlunoAtendimentoPort, usuarioId: UUID): Cadastro =
        port.resolverPorUsuario(usuarioId) ?: throw AcessoNegadoException(SEM_CADASTRO)
}
