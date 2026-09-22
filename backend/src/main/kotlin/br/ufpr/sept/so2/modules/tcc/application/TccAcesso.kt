package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.modules.tcc.application.ports.AlunoTccPort
import br.ufpr.sept.so2.modules.tcc.application.ports.AlunoTccPort.AlunoRef
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import java.util.UUID

internal object TccAcesso {
    const val VIEW = "tcc.view_own"
    const val REVIEW = "tcc.review"

    fun exigirAlunoAtivo(port: AlunoTccPort, usuarioId: UUID): AlunoRef {
        val aluno = port.resolver(usuarioId)
            ?: throw AcessoNegadoException("Cadastro acadêmico de aluno não encontrado.")
        if (aluno.egresso) {
            throw AcessoNegadoException("Egresso não acessa TCCs.")
        }
        return aluno
    }

    fun exigirDono(tcc: Tcc, alunoId: UUID) {
        if (!tcc.pertenceAoAluno(alunoId)) {
            throw RecursoNaoEncontradoException("TCC não encontrado.")
        }
    }

    fun exigirMembro(tcc: Tcc, usuarioId: UUID) {
        if (tcc.membroDe(usuarioId) == null) {
            throw RecursoNaoEncontradoException("TCC não encontrado.")
        }
    }
}
