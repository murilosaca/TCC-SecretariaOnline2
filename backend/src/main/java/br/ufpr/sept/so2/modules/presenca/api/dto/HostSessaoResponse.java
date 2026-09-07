package br.ufpr.sept.so2.modules.presenca.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HostSessaoResponse(
        UUID eventoId,
        String titulo,
        String attendanceMode,
        String estado,
        boolean janelaAtiva,
        OffsetDateTime janelaExpira,
        String pin,
        long presentes,
        @JsonProperty("_links") Map<String, String> links
) {
}
