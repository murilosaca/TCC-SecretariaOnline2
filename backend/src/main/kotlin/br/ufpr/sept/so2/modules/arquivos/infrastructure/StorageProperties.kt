package br.ufpr.sept.so2.modules.arquivos.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.storage")
data class StorageProperties(
    val mode: String = "s3",
    val endpoint: String = "http://localhost:9000",
    val publicEndpoint: String = "http://localhost:9000",
    val accessKey: String = "minioadmin",
    val secretKey: String = "minioadmin",
    val bucket: String = "so2",
    val region: String = "us-east-1",
    val pathStyle: Boolean = true,
    val downloadTtlSeconds: Long = 900,
    val uploadTtlSeconds: Long = 300,
) {
    fun isMemory(): Boolean = mode.equals("memory", ignoreCase = true)
}
