package br.ufpr.sept.so2.modules.iam.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RedefinirSenhaRequest(
        @NotBlank(message = "Token obrigatório") String token,
        @NotBlank(message = "Informe a nova senha") String novaSenha
) {
}
