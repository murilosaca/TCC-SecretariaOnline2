package br.ufpr.sept.so2.modules.solicitacoes.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class RequestTypeResponse(
    val id: UUID,
    val codigo: String,
    val nome: String,
    val descricao: String?,
    val status: String,
    val prazoDias: Int,
    val versao: Int,
    val formSchema: Map<String, Any?>,
    val workflowJson: Map<String, Any?>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)
