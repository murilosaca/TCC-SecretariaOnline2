package br.ufpr.sept.so2.modules.solicitacoes.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

public record RequestTypeResponse(
        UUID id,
        String codigo,
        String nome,
        String descricao,
        String status,
        int prazoDias,
        int versao,
        Map<String, Object> formSchema,
        Map<String, Object> workflowJson,
        @JsonProperty("_links") Map<String, String> links
) {
}
