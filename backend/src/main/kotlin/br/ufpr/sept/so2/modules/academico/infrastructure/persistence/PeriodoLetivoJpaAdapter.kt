package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository
import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@Component
class PeriodoLetivoJpaAdapter(
    private val jpaRepository: PeriodoLetivoJpaRepository,
) : PeriodoLetivoRepository {
    override fun save(periodo: PeriodoLetivo): PeriodoLetivo {
        val entity = jpaRepository.findById(periodo.id).orElseGet { PeriodoLetivoJpaEntity.fromDomain(periodo) }
        entity.merge(periodo)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Optional<PeriodoLetivo> =
        jpaRepository.findById(id).map { it.toDomain() }

    override fun findAll(pageable: Pageable): Page<PeriodoLetivo> =
        jpaRepository.findAll(pageable).map { it.toDomain() }

    override fun findAll(): List<PeriodoLetivo> =
        jpaRepository.findAll().map { it.toDomain() }

    override fun findVigente(data: LocalDate): Optional<PeriodoLetivo> =
        jpaRepository.findVigente(data).map { it.toDomain() }

    override fun existsByAnoAndSemestre(ano: Int, semestre: Int): Boolean =
        jpaRepository.existsByAnoAndSemestre(ano.toShort(), semestre.toShort())

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }
}
