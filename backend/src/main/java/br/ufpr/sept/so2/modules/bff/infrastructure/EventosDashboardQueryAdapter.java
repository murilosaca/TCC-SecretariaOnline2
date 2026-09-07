package br.ufpr.sept.so2.modules.bff.infrastructure;

import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort;
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Component
public class EventosDashboardQueryAdapter implements EventosDashboardQueryPort {

    private static final int PROXIMOS_LIMITE = 3;

    private final EventoRepository eventoRepository;

    public EventosDashboardQueryAdapter(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EventosDashboard consultar(OffsetDateTime agora) {
        OffsetDateTime inicioDia = agora.toLocalDate().atStartOfDay().atOffset(agora.getOffset());
        OffsetDateTime fimDia = inicioDia.plusDays(1);
        List<EventoResumo> doDia = eventoRepository.findEmAndamentoComJanelaNoDia(inicioDia, fimDia)
                .stream()
                .sorted(Comparator.comparing(Evento::getInicioEm))
                .map(evento -> toResumo(evento, agora))
                .toList();
        return new EventosDashboard(doDia.size(), doDia.stream().limit(PROXIMOS_LIMITE).toList());
    }

    private static EventoResumo toResumo(Evento evento, OffsetDateTime agora) {
        return new EventoResumo(
                evento.getId(),
                evento.getTitulo(),
                evento.getInicioEm(),
                evento.getFimEm(),
                evento.janelaAtiva(FasePresenca.ENTRADA, agora)
        );
    }
}
