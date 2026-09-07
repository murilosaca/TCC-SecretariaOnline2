package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class FormSchemaValidator(
    private val objectMapper: ObjectMapper,
) {

    fun validar(formSchemaJson: String?, payload: Map<String, Any?>?) {
        val schema: JsonNode
        val dados: JsonNode
        try {
            schema = objectMapper.readTree(formSchemaJson)
            dados = objectMapper.valueToTree(payload ?: emptyMap<String, Any?>())
        } catch (_: Exception) {
            throw DadoInvalidoException("form_schema ou payload inválido.")
        }
        if ("object" != schema.path("type").asText("object")) {
            throw DadoInvalidoException("form_schema deve descrever um objeto.")
        }
        val properties = schema.path("properties")
        if (schema.path("additionalProperties").isBoolean && !schema.get("additionalProperties").asBoolean()) {
            val permitidos = HashSet<String>()
            properties.fieldNames().forEachRemaining { permitidos.add(it) }
            dados.fieldNames().forEachRemaining { nome ->
                if (!permitidos.contains(nome)) {
                    throw DadoInvalidoException("Campo não previsto no formulário: $nome")
                }
            }
        }
        val obrigatorios = ArrayList<String>()
        schema.path("required").forEach { node -> obrigatorios.add(node.asText()) }
        for (campo in obrigatorios) {
            val valor = dados.get(campo)
            if (valor == null || valor.isNull || (valor.isTextual && valor.asText().isBlank())) {
                throw DadoInvalidoException("Campo obrigatório: $campo")
            }
        }
        properties.fields().forEachRemaining { entry ->
            val valor = dados.get(entry.key)
            if (valor != null && !valor.isNull) {
                validarCampo(entry.key, entry.value, valor)
            }
        }
    }

    companion object {
        private fun validarCampo(nome: String, schema: JsonNode, valor: JsonNode) {
            val tipo = schema.path("type").asText("string")
            when (tipo) {
                "string" -> {
                    if (!valor.isTextual) {
                        throw DadoInvalidoException("Campo $nome deve ser texto.")
                    }
                    val texto = valor.asText()
                    if (schema.has("minLength") && texto.length < schema.get("minLength").asInt()) {
                        throw DadoInvalidoException("Campo $nome é curto demais.")
                    }
                    if (schema.has("maxLength") && texto.length > schema.get("maxLength").asInt()) {
                        throw DadoInvalidoException("Campo $nome é longo demais.")
                    }
                }
                "number", "integer" -> {
                    if (!valor.isNumber) {
                        throw DadoInvalidoException("Campo $nome deve ser numérico.")
                    }
                    if ("integer" == tipo && !valor.isIntegralNumber) {
                        throw DadoInvalidoException("Campo $nome deve ser inteiro.")
                    }
                }
                "boolean" -> {
                    if (!valor.isBoolean) {
                        throw DadoInvalidoException("Campo $nome deve ser verdadeiro ou falso.")
                    }
                }
                else -> {
                    // tipos compostos ficam para versões futuras do motor
                }
            }
            if (schema.has("enum")) {
                var bate = false
                for (opcao in schema.get("enum")) {
                    if (opcao == valor) {
                        bate = true
                        break
                    }
                }
                if (!bate) {
                    throw DadoInvalidoException("Campo $nome possui valor fora da lista permitida.")
                }
            }
        }
    }
}
