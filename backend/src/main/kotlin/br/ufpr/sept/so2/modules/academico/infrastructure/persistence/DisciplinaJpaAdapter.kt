package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import br.ufpr.sept.so2.modules.academico.application.ports.DisciplinaRepository
import br.ufpr.sept.so2.modules.academico.domain.Disciplina
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
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
        val spec = Specification<DisciplinaJpaEntity> { root, _, cb ->
            val predicates = buildList<Predicate> {
                if (idCurso != null) {
                    add(cb.equal(root.get<UUID>("idCurso"), idCurso))
                }
                if (cursoIds != null) {
                    add(root.get<UUID>("idCurso").`in`(cursoIds))
                }
            }
            cb.and(*predicates.toTypedArray())
        }
        return jpaRepository.findAll(spec, pageable).map { it.toDomain() }
    }

    override fun existsByCursoAndCodigo(idCurso: UUID, codigo: String): Boolean =
        jpaRepository.existsByIdCursoAndCodigoIgnoreCase(idCurso, codigo)

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }
}
