package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Curso
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class CursoJpaAdapter(
    private val jpaRepository: CursoJpaRepository,
) : CursoRepository {
    override fun save(curso: Curso): Curso {
        val entity = jpaRepository.findById(curso.id).orElseGet { CursoJpaEntity.fromDomain(curso) }
        entity.merge(curso)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Optional<Curso> =
        jpaRepository.findById(id).map { it.toDomain() }

    override fun findAll(pageable: Pageable): Page<Curso> =
        jpaRepository.findAll(pageable).map { it.toDomain() }

    override fun existsBySigla(sigla: String): Boolean =
        jpaRepository.existsBySiglaIgnoreCase(sigla)

    override fun existsByCodigo(codigo: String): Boolean =
        jpaRepository.existsByCodigoIgnoreCase(codigo)

    override fun findByCodigo(codigo: String): Optional<Curso> =
        jpaRepository.findByCodigoIgnoreCase(codigo).map { it.toDomain() }

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }
}
