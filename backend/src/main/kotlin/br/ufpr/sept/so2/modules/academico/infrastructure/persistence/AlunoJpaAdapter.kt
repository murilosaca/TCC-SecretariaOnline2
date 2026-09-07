package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class AlunoJpaAdapter(
    private val jpaRepository: AlunoJpaRepository,
) : AlunoRepository {
    override fun save(aluno: Aluno): Aluno {
        val entity = jpaRepository.findById(aluno.id).orElseGet { AlunoJpaEntity.fromDomain(aluno) }
        entity.merge(aluno)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Optional<Aluno> =
        jpaRepository.findById(id).map { it.toDomain() }

    override fun findByGrr(grr: String): Optional<Aluno> =
        jpaRepository.findByGrrIgnoreCase(grr).map { it.toDomain() }

    override fun findByEmailInstitucional(email: String): Optional<Aluno> =
        jpaRepository.findByEmailInstitucionalIgnoreCase(email).map { it.toDomain() }

    override fun findAll(idCurso: UUID?, termo: String?, pageable: Pageable): Page<Aluno> {
        val spec = Specification<AlunoJpaEntity> { root, _, cb ->
            val predicates = buildList<Predicate> {
                if (idCurso != null) {
                    add(cb.equal(root.get<UUID>("idCurso"), idCurso))
                }
                if (!termo.isNullOrBlank()) {
                    val like = "%${termo.trim().lowercase()}%"
                    add(
                        cb.or(
                            cb.like(cb.lower(root.get("nome")), like),
                            cb.like(cb.lower(root.get("grr")), like),
                            cb.like(cb.lower(root.get("emailInstitucional")), like),
                        ),
                    )
                }
            }
            cb.and(*predicates.toTypedArray())
        }
        return jpaRepository.findAll(spec, pageable).map { it.toDomain() }
    }

    override fun existsByGrr(grr: String): Boolean = jpaRepository.existsByGrrIgnoreCase(grr)

    override fun existsByEmailInstitucional(email: String): Boolean =
        jpaRepository.existsByEmailInstitucionalIgnoreCase(email)

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }
}
