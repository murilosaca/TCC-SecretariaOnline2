package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort
import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort.AlunoRef
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import java.util.UUID

internal object EstagioAcesso {
    const val VIEW = "internship.view_own"
    const val REVIEW = "internship.review"

    fun exigirAlunoAtivo(port: AlunoEstagioPort, usuarioId: UUID): AlunoRef {
        val aluno = port.resolver(usuarioId)
            ?: throw AcessoNegadoException("Cadastro acadêmico de aluno não encontrado.")
        if (aluno.egresso) {
            throw AcessoNegadoException("Egresso não acessa estágios.")
        }
        return aluno
    }

    fun exigirDono(estagio: Estagio, alunoId: UUID) {
        if (!estagio.pertenceAoAluno(alunoId)) {
            throw RecursoNaoEncontradoException("Estágio não encontrado.")
        }
    }

    fun exigirOrientador(estagio: Estagio, usuarioId: UUID) {
        if (!estagio.orientadoPor(usuarioId)) {
            throw RecursoNaoEncontradoException("Estágio não encontrado.")
        }
    }
}
