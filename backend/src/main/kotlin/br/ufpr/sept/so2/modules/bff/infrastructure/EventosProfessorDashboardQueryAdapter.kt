package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.bff.application.ports.EventosProfessorDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.EventosProfessorDashboardQueryPort.EventosProfessorDashboard
import br.ufpr.sept.so2.modules.bff.application.ports.EventosProfessorDashboardQueryPort.MeuEventoResumo
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Component
class EventosProfessorDashboardQueryAdapter(
    private val eventoRepository: EventoRepository,
) : EventosProfessorDashboardQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(anfitriaoId: UUID, agora: OffsetDateTime): EventosProfessorDashboard {
        val inicioDia = agora.toLocalDate().atStartOfDay().atOffset(agora.offset)
        val fimDia = inicioDia.plusDays(1)
        val page = eventoRepository.findByAnfitriao(
            anfitriaoId,
            PageRequest.of(0, JANELA, Sort.by(Sort.Direction.ASC, "inicioEm")),
        )
        val doDia = page.content
            .filter { sobrepoeDia(it, inicioDia, fimDia) }
            .sortedBy { it.inicioEm }
        return EventosProfessorDashboard(
            doDia.size,
            doDia.take(MEUS_LIMITE).map(::toResumo),
        )
    }

    companion object {
        private const val JANELA = 50
        private const val MEUS_LIMITE = 5

        private fun sobrepoeDia(evento: Evento, inicioDia: OffsetDateTime, fimDia: OffsetDateTime): Boolean =
            evento.inicioEm.isBefore(fimDia) && !evento.fimEm.isBefore(inicioDia)

        private fun toResumo(evento: Evento): MeuEventoResumo {
            val links = linkedMapOf("self" to "/professor/eventos/${evento.id}")
            if (evento.estado == EventoEstado.EM_ANDAMENTO) {
                links["operar"] = "/professor/eventos/${evento.id}/operacao"
            }
            return MeuEventoResumo(
                evento.id,
                evento.titulo,
                evento.inicioEm,
                evento.fimEm,
                evento.estado.name,
                links,
            )
        }
    }
}
