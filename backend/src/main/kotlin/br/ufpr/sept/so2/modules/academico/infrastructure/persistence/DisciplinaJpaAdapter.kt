package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import br.ufpr.sept.so2.modules.academico.application.ports.DisciplinaRepository
import br.ufpr.sept.so2.modules.academico.domain.Disciplina
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DisciplinaJpaAdapter(
    private val jpaRepository: DisciplinaJpaRepository,
) : DisciplinaRepository {
    override fun save(disciplina: Disciplina): Disciplina {
        val entity = jpaRepository.findById(disciplina.id).orElseGet { DisciplinaJpaEntity.fromDomain(disciplina) }
        entity.merge(disciplina)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Disciplina? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findAll(idCurso: UUID?, cursoIds: Collection<UUID>?, pageable: Pageable): Page<Disciplina> {
        if (cursoIds != null && cursoIds.isEmpty()) {
            return Page.empty(pageable)
        }
        if (idCurso != null) {
            return jpaRepository.findByIdCurso(idCurso, pageable).map { it.toDomain() }
        }
        if (cursoIds != null) {
            return jpaRepository.findByIdCursoIn(cursoIds, pageable).map { it.toDomain() }
        }
        return jpaRepository.findAll(pageable).map { it.toDomain() }
    }

    override fun existsByCursoAndCodigo(idCurso: UUID, codigo: String): Boolean =
        jpaRepository.existsByIdCursoAndCodigoIgnoreCase(idCurso, codigo)

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }
}
