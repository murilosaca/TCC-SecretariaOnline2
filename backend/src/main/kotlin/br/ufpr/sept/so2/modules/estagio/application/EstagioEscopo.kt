package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort
import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort.Cadastro
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import java.util.UUID

internal object EstagioEscopo {
    fun cursos(port: CursoEscopoPort, atorId: UUID): Set<UUID> = port.cursoIdsDoUsuario(atorId)

    fun exigirAluno(cursos: Set<UUID>, port: AlunoEstagioPort, alunoId: UUID): Cadastro {
        val aluno = port.cadastro(alunoId)
        if (aluno == null || aluno.idCurso !in cursos) {
            throw RecursoNaoEncontradoException("Aluno não encontrado.")
        }
        return aluno
    }

    fun exigirEstagio(cursos: Set<UUID>, repository: EstagioRepository, estagioId: UUID): Estagio {
        val estagio = repository.findById(estagioId)
            ?: throw RecursoNaoEncontradoException("Estágio não encontrado.")
        if (estagio.idCurso !in cursos) {
            throw RecursoNaoEncontradoException("Estágio não encontrado.")
        }
        return estagio
    }
}
