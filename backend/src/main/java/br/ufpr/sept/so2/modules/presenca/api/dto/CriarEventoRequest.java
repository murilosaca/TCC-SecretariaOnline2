package br.ufpr.sept.so2.modules.presenca.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

public record CriarEventoRequest(
        @NotBlank String titulo,
        @NotNull OffsetDateTime inicioEm,
        @NotNull OffsetDateTime fimEm,
        @Positive int cargaHoraria
) {
}
