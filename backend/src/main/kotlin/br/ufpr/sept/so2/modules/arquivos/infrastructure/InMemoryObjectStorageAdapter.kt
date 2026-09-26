package br.ufpr.sept.so2.modules.arquivos.infrastructure

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Storage em memória para testes (profile test / STORAGE_MODE=memory).
 * Não usa filesystem local.
 */
@Component
@ConditionalOnProperty(prefix = "app.storage", name = ["mode"], havingValue = "memory")
class InMemoryObjectStorageAdapter(
    private val properties: StorageProperties,
) : ObjectStoragePort {

    private val objects = ConcurrentHashMap<String, StoredObject>()

    override fun putObject(key: String, contentType: String, bytes: ByteArray) {
        objects[key] = StoredObject(contentType, bytes.copyOf())
    }

    override fun getObject(key: String): ByteArray? = objects[key]?.bytes?.copyOf()

    override fun presignGetUrl(key: String, ttl: Duration, fileName: String?): String {
        require(objects.containsKey(key)) { "Objeto não encontrado: $key" }
        val expira = Instant.now().plus(ttl).epochSecond
        val nome = fileName?.let { "&fileName=${encode(it)}" }.orEmpty()
        return "${properties.publicEndpoint.trimEnd('/')}/${properties.bucket}/$key?X-Amz-Expires=${ttl.seconds}&expires=$expira$nome"
    }

    override fun presignPutUrl(key: String, contentType: String, ttl: Duration): String {
        val expira = Instant.now().plus(ttl).epochSecond
        return "${properties.publicEndpoint.trimEnd('/')}/${properties.bucket}/$key?X-Amz-Expires=${ttl.seconds}&expires=$expira&contentType=${encode(contentType)}"
    }

    override fun exists(key: String): Boolean = objects.containsKey(key)

    fun clear() {
        objects.clear()
    }

    private data class StoredObject(val contentType: String, val bytes: ByteArray)

    companion object {
        private fun encode(value: String): String =
            URLEncoder.encode(value, StandardCharsets.UTF_8)
    }
}
