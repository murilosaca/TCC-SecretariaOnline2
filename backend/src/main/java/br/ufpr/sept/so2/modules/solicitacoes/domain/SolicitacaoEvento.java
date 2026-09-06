package br.ufpr.sept.so2.modules.solicitacoes.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SolicitacaoEvento(
        UUID id,
        String tipo,
        String estadoDe,
        String estadoPara,
        UUID atorId,
        String parecer,
        String payload,
        OffsetDateTime createdAt
) {
}
