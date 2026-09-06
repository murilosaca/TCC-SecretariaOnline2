package br.ufpr.sept.so2.modules.iam.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarSenhaRequest(
        @NotBlank(message = "Informe um e-mail")
        @Email(message = "Informe um e-mail válido")
        String email
) {
}
