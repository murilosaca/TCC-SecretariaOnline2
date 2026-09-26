package br.ufpr.sept.so2.modules.arquivos.api.dto

data class DownloadUrlResponse(
    val downloadUrl: String,
    val expiresInSeconds: Long = 900,
)
