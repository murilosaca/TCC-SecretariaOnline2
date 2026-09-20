package br.ufpr.sept.so2.shared

import com.fasterxml.jackson.databind.ObjectMapper

object ItJson {
    private val mapper = ObjectMapper()

    fun text(json: String, field: String): String {
        val node = mapper.readTree(json).get(field)
        require(node != null && !node.isNull) { "Campo $field ausente no JSON" }
        return node.asText()
    }
}
