package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort.EventoResumo
import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort.EventosDashboard
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Component
class EventosDashboardQueryAdapter(
    private val eventoRepository: EventoRepository,
) : EventosDashboardQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(agora: OffsetDateTime): EventosDashboard {
        val inicioDia = agora.toLocalDate().atStartOfDay().atOffset(agora.offset)
        val fimDia = inicioDia.plusDays(1)
        val doDia = eventoRepository.findEmAndamentoComJanelaNoDia(inicioDia, fimDia)
            .sortedBy { it.inicioEm }
            .map { toResumo(it, agora) }
        return EventosDashboard(doDia.size, doDia.take(PROXIMOS_LIMITE))
    }

    companion object {
        private const val PROXIMOS_LIMITE = 3

        private fun toResumo(evento: Evento, agora: OffsetDateTime): EventoResumo =
            EventoResumo(
                evento.id,
                evento.titulo,
                evento.inicioEm,
                evento.fimEm,
                evento.janelaAtiva(FasePresenca.ENTRADA, agora),
            )
    }
}
