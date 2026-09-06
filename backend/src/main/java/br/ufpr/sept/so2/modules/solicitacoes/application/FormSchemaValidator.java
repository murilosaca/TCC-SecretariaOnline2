package br.ufpr.sept.so2.modules.solicitacoes.application;

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FormSchemaValidator {

    private final ObjectMapper objectMapper;

    public FormSchemaValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void validar(String formSchemaJson, Map<String, Object> payload) {
        JsonNode schema;
        JsonNode dados;
        try {
            schema = objectMapper.readTree(formSchemaJson);
            dados = objectMapper.valueToTree(payload == null ? Map.of() : payload);
        } catch (Exception ex) {
            throw new DadoInvalidoException("form_schema ou payload inválido.");
        }
        if (!"object".equals(schema.path("type").asText("object"))) {
            throw new DadoInvalidoException("form_schema deve descrever um objeto.");
        }
        JsonNode properties = schema.path("properties");
        if (schema.path("additionalProperties").isBoolean() && !schema.get("additionalProperties").asBoolean()) {
            Set<String> permitidos = new HashSet<>();
            properties.fieldNames().forEachRemaining(permitidos::add);
            dados.fieldNames().forEachRemaining(nome -> {
                if (!permitidos.contains(nome)) {
                    throw new DadoInvalidoException("Campo não previsto no formulário: " + nome);
                }
            });
        }
        List<String> obrigatorios = new ArrayList<>();
        schema.path("required").forEach(node -> obrigatorios.add(node.asText()));
        for (String campo : obrigatorios) {
            JsonNode valor = dados.get(campo);
            if (valor == null || valor.isNull() || (valor.isTextual() && valor.asText().isBlank())) {
                throw new DadoInvalidoException("Campo obrigatório: " + campo);
            }
        }
        properties.fields().forEachRemaining(entry -> {
            JsonNode valor = dados.get(entry.getKey());
            if (valor != null && !valor.isNull()) {
                validarCampo(entry.getKey(), entry.getValue(), valor);
            }
        });
    }

    private static void validarCampo(String nome, JsonNode schema, JsonNode valor) {
        String tipo = schema.path("type").asText("string");
        switch (tipo) {
            case "string" -> {
                if (!valor.isTextual()) {
                    throw new DadoInvalidoException("Campo " + nome + " deve ser texto.");
                }
                String texto = valor.asText();
                if (schema.has("minLength") && texto.length() < schema.get("minLength").asInt()) {
                    throw new DadoInvalidoException("Campo " + nome + " é curto demais.");
                }
                if (schema.has("maxLength") && texto.length() > schema.get("maxLength").asInt()) {
                    throw new DadoInvalidoException("Campo " + nome + " é longo demais.");
                }
            }
            case "number", "integer" -> {
                if (!valor.isNumber()) {
                    throw new DadoInvalidoException("Campo " + nome + " deve ser numérico.");
                }
                if ("integer".equals(tipo) && !valor.isIntegralNumber()) {
                    throw new DadoInvalidoException("Campo " + nome + " deve ser inteiro.");
                }
            }
            case "boolean" -> {
                if (!valor.isBoolean()) {
                    throw new DadoInvalidoException("Campo " + nome + " deve ser verdadeiro ou falso.");
                }
            }
            default -> {
                // tipos compostos ficam para versões futuras do motor
            }
        }
        if (schema.has("enum")) {
            boolean bate = false;
            for (JsonNode opcao : schema.get("enum")) {
                if (opcao.equals(valor)) {
                    bate = true;
                    break;
                }
            }
            if (!bate) {
                throw new DadoInvalidoException("Campo " + nome + " possui valor fora da lista permitida.");
            }
        }
    }
}
