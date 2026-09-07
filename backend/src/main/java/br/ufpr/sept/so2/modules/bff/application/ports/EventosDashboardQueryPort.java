package br.ufpr.sept.so2.modules.bff.application.ports;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface EventosDashboardQueryPort {

    EventosDashboard consultar(OffsetDateTime agora);

    record EventosDashboard(int hoje, List<EventoResumo> proximos) {
    }

    record EventoResumo(
            UUID id,
            String titulo,
            OffsetDateTime inicioEm,
            OffsetDateTime fimEm,
            boolean janelaAtiva
    ) {
    }
}
