package br.ufpr.sept.so2.modules.diplomas.application.ports

import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.modules.diplomas.domain.DiplomaSituacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface DiplomaRepository {
    fun save(diploma: Diploma): Diploma

    fun findById(id: UUID): Diploma?

    fun findByAluno(alunoId: UUID): Diploma?

    fun existsByAluno(alunoId: UUID): Boolean

    fun findByCurso(
        cursoId: UUID,
        situacao: DiplomaSituacao?,
        pageable: Pageable,
    ): Page<Diploma>

    /** Lista de egressos da secretaria (F5.10): cursos do escopo, ano de colação e situação. */
    fun findByCursos(
        cursoIds: Collection<UUID>,
        ano: Int?,
        situacao: DiplomaSituacao?,
        pageable: Pageable,
    ): Page<Diploma>
}
