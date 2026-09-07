package br.ufpr.sept.so2.modules.academico.application.ports

import br.ufpr.sept.so2.modules.academico.domain.Aluno
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

interface AlunoRepository {
    fun save(aluno: Aluno): Aluno

    fun findById(id: UUID): Optional<Aluno>

    fun findByGrr(grr: String): Optional<Aluno>

    fun findByEmailInstitucional(email: String): Optional<Aluno>

    fun findAll(idCurso: UUID?, termo: String?, pageable: Pageable): Page<Aluno>

    fun existsByGrr(grr: String): Boolean

    fun existsByEmailInstitucional(email: String): Boolean

    fun deleteById(id: UUID)
}
