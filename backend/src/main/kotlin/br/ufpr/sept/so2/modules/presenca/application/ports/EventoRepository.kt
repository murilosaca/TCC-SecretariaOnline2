package br.ufpr.sept.so2.modules.presenca.application.ports

import br.ufpr.sept.so2.modules.presenca.domain.Evento
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

interface EventoRepository {
    fun save(evento: Evento): Evento

    fun findById(id: UUID): Optional<Evento>

    fun findAbertosParaAluno(agora: OffsetDateTime, pageable: Pageable): Page<Evento>

    fun findEmAndamentoComJanelaNoDia(inicioDia: OffsetDateTime, fimDia: OffsetDateTime): List<Evento>

    fun findByAnfitriao(anfitriaoId: UUID, pageable: Pageable): Page<Evento>

    fun existsByTitulo(titulo: String): Boolean

    fun findByTitulo(titulo: String): Optional<Evento>
}
