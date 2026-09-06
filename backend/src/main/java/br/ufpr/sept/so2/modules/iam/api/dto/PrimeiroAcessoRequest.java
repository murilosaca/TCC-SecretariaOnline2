package br.ufpr.sept.so2.modules.iam.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PrimeiroAcessoRequest(
        @NotBlank(message = "Informe a nova senha") String novaSenha,
        @NotNull(message = "Aceite a política de privacidade") Boolean aceiteTermos
) {
}
