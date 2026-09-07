package br.ufpr.sept.so2.modules.bff.application.ports;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SolicitacoesDashboardQueryPort {

    SolicitacoesDashboard consultar(UUID solicitanteId);

    record SolicitacoesDashboard(
            int abertas,
            List<PendenciaResumo> pendencias,
            List<SolicitacaoResumo> ultimas
    ) {
    }

    record PendenciaResumo(UUID id, String titulo, String estado, String href) {
    }

    record SolicitacaoResumo(
            UUID id,
            String protocolo,
            String tipoNome,
            String estado,
            OffsetDateTime prazoEm,
            boolean slaVencido
    ) {
    }
}
