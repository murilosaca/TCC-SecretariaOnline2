package br.ufpr.sept.so2.modules.iam.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Informe e-mail ou GRR") String identificador,
        @NotBlank(message = "Informe a senha") String senha
) {
}
