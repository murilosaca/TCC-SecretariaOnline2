package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.bff.application.SecretariaDashboardRegras
import br.ufpr.sept.so2.modules.bff.application.ports.AgendaDiaQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.AgendaDiaQueryPort.AgendaDia
import br.ufpr.sept.so2.modules.bff.application.ports.AgendaDiaQueryPort.Item
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Component
class AgendaDiaQueryAdapter(
    private val eventoRepository: EventoRepository,
) : AgendaDiaQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(agora: OffsetDateTime): AgendaDia {
        val inicio = SecretariaDashboardRegras.inicioDoDia(agora)
        val eventos = eventoRepository.findSobrepondoIntervalo(inicio, inicio.plusDays(1))
        return AgendaDia(
            eventos.size,
            eventos.take(SecretariaDashboardRegras.AGENDA_LIMITE).map(::toItem),
        )
    }

    companion object {
        private fun toItem(evento: Evento): Item =
            Item(evento.id, evento.titulo, evento.inicioEm, evento.fimEm, evento.estado.name)
    }
}
