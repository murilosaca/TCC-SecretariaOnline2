package br.ufpr.sept.so2.modules.diplomas.infrastructure.persistence

import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.modules.diplomas.domain.DiplomaSituacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DiplomaJpaAdapter(
    private val jpaRepository: DiplomaJpaRepository,
) : DiplomaRepository {
    override fun save(diploma: Diploma): Diploma {
        val entity = jpaRepository.findById(diploma.id).orElseGet { DiplomaJpaEntity.fromDomain(diploma) }
        entity.merge(diploma)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Diploma? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findByAluno(alunoId: UUID): Diploma? =
        jpaRepository.findByIdAluno(alunoId).map { it.toDomain() }.orElse(null)

    override fun existsByAluno(alunoId: UUID): Boolean =
        jpaRepository.existsByIdAluno(alunoId)

    override fun findByCurso(
        cursoId: UUID,
        situacao: DiplomaSituacao?,
        pageable: Pageable,
    ): Page<Diploma> =
        jpaRepository.findByCurso(cursoId, situacao?.name, pageable).map { it.toDomain() }
}
