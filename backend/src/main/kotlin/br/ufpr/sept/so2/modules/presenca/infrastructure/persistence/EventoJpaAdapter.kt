package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence

import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

@Component
class EventoJpaAdapter(
    private val jpaRepository: EventoJpaRepository,
) : EventoRepository {

    override fun save(evento: Evento): Evento {
        val entity = jpaRepository.findById(evento.id)
            .orElseGet { EventoJpaEntity.fromDomain(evento) }
        entity.merge(evento)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Optional<Evento> = jpaRepository.findById(id).map { it.toDomain() }

    override fun findAbertosParaAluno(agora: OffsetDateTime, pageable: Pageable): Page<Evento> =
        jpaRepository.findAbertosParaAluno(agora, pageable).map { it.toDomain() }

    override fun findEmAndamentoComJanelaNoDia(inicioDia: OffsetDateTime, fimDia: OffsetDateTime): List<Evento> =
        jpaRepository.findEmAndamentoComJanelaNoDia(inicioDia, fimDia).map { it.toDomain() }

    override fun findByAnfitriao(anfitriaoId: UUID, pageable: Pageable): Page<Evento> =
        jpaRepository.findByIdAnfitriao(anfitriaoId, pageable).map { it.toDomain() }

    override fun existsByTitulo(titulo: String): Boolean = jpaRepository.existsByTituloIgnoreCase(titulo)

    override fun findByTitulo(titulo: String): Optional<Evento> =
        jpaRepository.findByTituloIgnoreCase(titulo).map { it.toDomain() }
}
