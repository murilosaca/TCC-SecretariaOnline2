package br.ufpr.sept.so2.modules.solicitacoes.api.dto;

import br.ufpr.sept.so2.modules.solicitacoes.domain.SolicitacaoEvento;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SolicitacaoEventoResponse(
        UUID id,
        String tipo,
        String estadoDe,
        String estadoPara,
        UUID atorId,
        String parecer,
        OffsetDateTime createdAt
) {

    public static SolicitacaoEventoResponse from(SolicitacaoEvento evento) {
        return new SolicitacaoEventoResponse(
                evento.id(),
                evento.tipo(),
                evento.estadoDe(),
                evento.estadoPara(),
                evento.atorId(),
                evento.parecer(),
                evento.createdAt()
        );
    }
}
