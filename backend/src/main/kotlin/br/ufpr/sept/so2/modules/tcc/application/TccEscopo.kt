package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.tcc.application.ports.AlunoTccPort
import br.ufpr.sept.so2.modules.tcc.application.ports.AlunoTccPort.Cadastro
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import java.util.UUID

internal object TccEscopo {
    fun cursos(port: CursoEscopoPort, atorId: UUID): Set<UUID> = port.cursoIdsDoUsuario(atorId)

    fun exigirAluno(cursos: Set<UUID>, port: AlunoTccPort, alunoId: UUID): Cadastro {
        val aluno = port.cadastro(alunoId)
        if (aluno == null || aluno.idCurso !in cursos) {
            throw RecursoNaoEncontradoException("Aluno não encontrado.")
        }
        return aluno
    }

    fun exigirTcc(cursos: Set<UUID>, repository: TccRepository, tccId: UUID): Tcc {
        val tcc = repository.findById(tccId)
            ?: throw RecursoNaoEncontradoException("TCC não encontrado.")
        if (tcc.idCurso !in cursos) {
            throw RecursoNaoEncontradoException("TCC não encontrado.")
        }
        return tcc
    }
}
