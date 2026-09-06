package br.ufpr.sept.so2.modules.solicitacoes.application;

import br.ufpr.sept.so2.modules.solicitacoes.domain.WorkflowDefinicao;
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WorkflowJsonParser {

    private final ObjectMapper objectMapper;

    public WorkflowJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public WorkflowDefinicao parse(String workflowJson) {
        try {
            JsonNode root = objectMapper.readTree(workflowJson);
            String inicial = root.path("initial").asText(null);
            Map<String, Map<String, String>> transicoes = new LinkedHashMap<>();
            root.path("states").fields().forEachRemaining(estado -> {
                Map<String, String> acoes = new LinkedHashMap<>();
                estado.getValue().path("on").fields()
                        .forEachRemaining(acao -> acoes.put(acao.getKey(), acao.getValue().asText()));
                transicoes.put(estado.getKey(), acoes);
            });
            return new WorkflowDefinicao(inicial, transicoes);
        } catch (DadoInvalidoException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DadoInvalidoException("workflow_json inválido.");
        }
    }
}
