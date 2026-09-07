package br.ufpr.sept.so2.modules.academico.application.ports

import br.ufpr.sept.so2.modules.academico.domain.Disciplina
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface DisciplinaRepository {
    fun save(disciplina: Disciplina): Disciplina

    fun findById(id: UUID): Disciplina?

    fun findAll(idCurso: UUID?, pageable: Pageable): Page<Disciplina>

    fun existsByCursoAndCodigo(idCurso: UUID, codigo: String): Boolean

    fun deleteById(id: UUID)
}
