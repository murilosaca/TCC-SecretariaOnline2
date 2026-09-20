package br.ufpr.sept.so2.modules.solicitacoes.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.util.Collections
import java.util.LinkedHashMap

/**
 * Máquina de estados extraída de `workflow_json`.
 * Mapa: estado → (ação → estado destino).
 */
class WorkflowDefinicao(
    inicial: String?,
    transicoes: Map<String, Map<String, String>?>?,
) {
    val inicial: String
    private val transicoes: Map<String, Map<String, String>>

    init {
        if (inicial.isNullOrBlank()) {
            throw DadoInvalidoException("workflow_json.initial é obrigatório.")
        }
        if (transicoes.isNullOrEmpty()) {
            throw DadoInvalidoException("workflow_json.states é obrigatório.")
        }
        if (!transicoes.containsKey(inicial)) {
            throw DadoInvalidoException("Estado inicial ausente em workflow_json.states: $inicial")
        }
        val copia = LinkedHashMap<String, Map<String, String>>()
        transicoes.forEach { (estado, acoes) ->
            copia[estado] = if (acoes == null) emptyMap() else java.util.Map.copyOf(acoes)
        }
        this.inicial = inicial
        this.transicoes = Collections.unmodifiableMap(copia)
    }

    fun acoesDe(estado: String?): Set<String> =
        transicoes.getOrDefault(estado, emptyMap()).keys

    fun acoesDeliberativasDe(estado: String?): Set<String> =
        acoesDe(estado).filter { it in ACOES_DELIBERATIVAS }.toSet()

    fun estadosComAcoesDeliberativas(): Set<String> =
        transicoes.filter { (_, saidas) -> saidas.keys.any { it in ACOES_DELIBERATIVAS } }.keys

    fun resolverAcao(estadoAtual: String?, acao: String?): String {
        if (acao.isNullOrBlank()) {
            throw DadoInvalidoException("Ação de transição é obrigatória.")
        }
        val candidata = acao.trim().uppercase()
        val saidas = transicoes[estadoAtual].orEmpty()
        if (saidas.containsKey(candidata)) {
            return candidata
        }
        val alias = ALIAS_ACAO[candidata]
        if (alias != null && saidas.containsKey(alias)) {
            return alias
        }
        return candidata
    }

    fun transicionar(estadoAtual: String?, acao: String?): String {
        val resolvida = resolverAcao(estadoAtual, acao)
        val saidas = transicoes[estadoAtual]
        if (saidas == null || !saidas.containsKey(resolvida)) {
            throw ConflitoEstadoException(
                "Transição inválida: $estadoAtual --$resolvida-->",
            )
        }
        return saidas.getValue(resolvida)
    }

    companion object {
        val ACOES_DELIBERATIVAS: Set<String> = setOf(
            "DEFER",
            "INDEFER",
            "REQUEST_ADJUST",
            "REQUEST_ADJUSTMENT",
        )

        private val ALIAS_ACAO: Map<String, String> = mapOf(
            "REQUEST_ADJUSTMENT" to "REQUEST_ADJUST",
            "REQUEST_ADJUST" to "REQUEST_ADJUSTMENT",
        )
    }
}
