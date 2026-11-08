package br.ufpr.sept.so2.modules.solicitacoes.api.dto

data class AnexoSolicitacaoResponse(
    val nome: String,
    val tamanho: Int,
    val sha256: String,
    val downloadUrl: String,
)

data class ProtocoloPdfResponse(
    val downloadUrl: String,
    val hashSha256: String,
)

data class EditarPayloadRequest(
    val payload: Map<String, Any?>? = null,
)

data class DeliberarLoteRequest(
    val ids: List<java.util.UUID> = emptyList(),
    val action: String? = null,
    val parecer: String? = null,
)
