package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.shared.domain.exception.SchemaInvalidoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class TipoSolicitacaoSchemaValidator(
    private val objectMapper: ObjectMapper,
    private val workflowJsonParser: WorkflowJsonParser,
) {
    fun validar(formSchema: String?, workflowJson: String?) {
        val form = texto(formSchema, "form_schema")
        val fluxo = texto(workflowJson, "workflow_json")
        lerObjeto(form, "form_schema", "invalid-schema")
        try {
            lerObjeto(fluxo, "workflow_json", "invalid-workflow")
            workflowJsonParser.parse(fluxo)
        } catch (ex: SchemaInvalidoException) {
            throw ex
        } catch (ex: RuntimeException) {
            throw SchemaInvalidoException("invalid-workflow", ex.message ?: "workflow_json inválido.")
        }
    }

    private fun texto(valor: String?, campo: String): String {
        if (valor.isNullOrBlank()) {
            throw SchemaInvalidoException(
                if (campo == "form_schema") "invalid-schema" else "invalid-workflow",
                "$campo: SyntaxError at line 1",
            )
        }
        return valor
    }

    private fun lerObjeto(json: String, campo: String, tipo: String) {
        try {
            val node = objectMapper.readTree(json)
            if (node == null || !node.isObject) {
                throw SchemaInvalidoException(tipo, "$campo: JSON deve ser um objeto.")
            }
        } catch (ex: SchemaInvalidoException) {
            throw ex
        } catch (ex: JsonProcessingException) {
            val linha = ex.location?.lineNr?.takeIf { it > 0 } ?: 1
            throw SchemaInvalidoException(tipo, "$campo: SyntaxError at line $linha")
        }
    }
}
