package br.ufpr.sept.so2.modules.academico.api.dto;

import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record PeriodoLetivoResponse(
        UUID id,
        int ano,
        int semestre,
        LocalDate inicio,
        LocalDate fim,
        boolean ativo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        @JsonProperty("_links") Map<String, String> links
) {

    public static PeriodoLetivoResponse from(PeriodoLetivo periodo) {
        String base = "/academico/periodos/" + periodo.getId();
        return new PeriodoLetivoResponse(
                periodo.getId(),
                periodo.getAno(),
                periodo.getSemestre(),
                periodo.getInicio(),
                periodo.getFim(),
                periodo.isAtivo(),
                periodo.getCreatedAt(),
                periodo.getUpdatedAt(),
                Map.of(
                        "self", base,
                        "atualizar", base,
                        "excluir", base
                )
        );
    }
}
