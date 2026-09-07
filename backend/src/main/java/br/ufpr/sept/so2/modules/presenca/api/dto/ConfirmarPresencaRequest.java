package br.ufpr.sept.so2.modules.presenca.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmarPresencaRequest(
        @NotBlank String pin,
        @NotBlank @Size(min = 8, max = 64) String deviceUuid,
        @NotBlank String fase
) {
}
