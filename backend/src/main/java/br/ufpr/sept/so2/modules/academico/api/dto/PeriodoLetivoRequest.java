package br.ufpr.sept.so2.modules.academico.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PeriodoLetivoRequest(
        @NotNull @Min(2000) @Max(2100) Integer ano,
        @NotNull @Min(1) @Max(2) Integer semestre,
        @NotNull LocalDate inicio,
        @NotNull LocalDate fim,
        Boolean ativo
) {
}
