package br.ufpr.sept.so2.modules.solicitacoes.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CriarSolicitacaoRequest(
        @NotBlank String tipoCodigo,
        @NotNull Map<String, Object> payload
) {
}
