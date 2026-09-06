package br.ufpr.sept.so2.modules.solicitacoes.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SolicitacaoResponse(
        UUID id,
        String protocolo,
        String tipoCodigo,
        String tipoNome,
        int tipoVersao,
        String estado,
        Map<String, Object> payload,
        Map<String, Object> formSchema,
        OffsetDateTime prazoEm,
        boolean prazoVencido,
        String sla,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<SolicitacaoEventoResponse> eventos,
        @JsonProperty("_links") Map<String, String> links
) {
}
