package br.ufpr.sept.so2.modules.presenca.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record SessaoPresencaResponse(
        UUID eventoId,
        String titulo,
        String attendanceMode,
        String estado,
        String situacaoPresenca,
        boolean janelaAtiva,
        OffsetDateTime janelaExpira,
        String faseDisponivel,
        @JsonProperty("_links") Map<String, String> links
) {
}
