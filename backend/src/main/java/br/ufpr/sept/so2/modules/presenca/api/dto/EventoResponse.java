package br.ufpr.sept.so2.modules.presenca.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record EventoResponse(
        UUID id,
        UUID idAnfitriao,
        String titulo,
        OffsetDateTime inicioEm,
        OffsetDateTime fimEm,
        int cargaHoraria,
        String attendanceMode,
        String estado,
        String situacaoPresenca,
        boolean janelaAtiva,
        @JsonProperty("_links") Map<String, String> links
) {
}
