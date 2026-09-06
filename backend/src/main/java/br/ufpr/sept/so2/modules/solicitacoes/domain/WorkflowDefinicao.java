package br.ufpr.sept.so2.modules.solicitacoes.domain;

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados extraída de {@code workflow_json}.
 * Mapa: estado → (ação → estado destino).
 */
public final class WorkflowDefinicao {

    private final String inicial;
    private final Map<String, Map<String, String>> transicoes;

    public WorkflowDefinicao(String inicial, Map<String, Map<String, String>> transicoes) {
        if (inicial == null || inicial.isBlank()) {
            throw new DadoInvalidoException("workflow_json.initial é obrigatório.");
        }
        if (transicoes == null || transicoes.isEmpty()) {
            throw new DadoInvalidoException("workflow_json.states é obrigatório.");
        }
        if (!transicoes.containsKey(inicial)) {
            throw new DadoInvalidoException("Estado inicial ausente em workflow_json.states: " + inicial);
        }
        Map<String, Map<String, String>> copia = new LinkedHashMap<>();
        transicoes.forEach((estado, acoes) ->
                copia.put(estado, acoes == null ? Map.of() : Map.copyOf(acoes)));
        this.inicial = inicial;
        this.transicoes = Collections.unmodifiableMap(copia);
    }

    public String getInicial() {
        return inicial;
    }

    public Set<String> acoesDe(String estado) {
        return transicoes.getOrDefault(estado, Map.of()).keySet();
    }

    public String transicionar(String estadoAtual, String acao) {
        if (acao == null || acao.isBlank()) {
            throw new DadoInvalidoException("Ação de transição é obrigatória.");
        }
        Map<String, String> saidas = transicoes.get(estadoAtual);
        if (saidas == null || !saidas.containsKey(acao)) {
            throw new ConflitoEstadoException(
                    "Transição inválida: " + estadoAtual + " --" + acao + "-->");
        }
        return saidas.get(acao);
    }
}
