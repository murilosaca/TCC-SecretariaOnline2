package br.ufpr.sept.so2.modules.arquivos.application.ports

import java.time.Duration

/**
 * Porta de armazenamento S3-compatível (RNF-POR-03).
 * A aplicação não grava em filesystem local.
 */
interface ObjectStoragePort {
    fun putObject(key: String, contentType: String, bytes: ByteArray)

    fun getObject(key: String): ByteArray?

    fun presignGetUrl(key: String, ttl: Duration = Duration.ofMinutes(15), fileName: String? = null): String

    fun presignPutUrl(key: String, contentType: String, ttl: Duration = Duration.ofMinutes(5)): String

    fun exists(key: String): Boolean
}
