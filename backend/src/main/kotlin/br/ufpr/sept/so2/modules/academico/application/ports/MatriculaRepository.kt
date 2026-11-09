package br.ufpr.sept.so2.modules.academico.application.ports

import br.ufpr.sept.so2.modules.academico.domain.Matricula
import java.util.UUID

interface MatriculaRepository {
    fun save(matricula: Matricula): Matricula

    fun exists(idAluno: UUID, idDisciplina: UUID, idPeriodoLetivo: UUID): Boolean
}
