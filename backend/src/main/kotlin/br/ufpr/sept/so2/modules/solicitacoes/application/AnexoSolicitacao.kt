package br.ufpr.sept.so2.modules.solicitacoes.application

data class AnexoSolicitacao(
    val nome: String,
    val contentType: String,
    val bytes: ByteArray,
    val sha256Informado: String?,
)
