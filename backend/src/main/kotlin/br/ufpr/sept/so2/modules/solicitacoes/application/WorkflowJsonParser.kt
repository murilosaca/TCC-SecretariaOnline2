package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.solicitacoes.domain.WorkflowDefinicao
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.util.LinkedHashMap

@Component
class WorkflowJsonParser(
    private val objectMapper: ObjectMapper,
) {

    fun parse(workflowJson: String?): WorkflowDefinicao {
        try {
            val root = objectMapper.readTree(workflowJson)
            val inicial = root.path("initial").asText(null)
            val transicoes = LinkedHashMap<String, Map<String, String>>()
            root.path("states").fields().forEachRemaining { estado ->
                val acoes = LinkedHashMap<String, String>()
                estado.value.path("on").fields()
                    .forEachRemaining { acao -> acoes[acao.key] = acao.value.asText() }
                transicoes[estado.key] = acoes
            }
            return WorkflowDefinicao(inicial, transicoes)
        } catch (ex: DadoInvalidoException) {
            throw ex
        } catch (_: Exception) {
            throw DadoInvalidoException("workflow_json inválido.")
        }
    }
}
