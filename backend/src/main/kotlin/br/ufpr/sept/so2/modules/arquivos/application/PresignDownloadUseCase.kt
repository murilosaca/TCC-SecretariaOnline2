package br.ufpr.sept.so2.modules.arquivos.application

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.arquivos.infrastructure.StorageProperties
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class PresignDownloadUseCase(
    private val objectStoragePort: ObjectStoragePort,
    private val storageProperties: StorageProperties,
) {
    fun execute(storageKey: String, fileName: String? = null): String {
        if (storageKey.isBlank()) {
            throw RecursoNaoEncontradoException("Arquivo não encontrado.")
        }
        if (!objectStoragePort.exists(storageKey)) {
            throw RecursoNaoEncontradoException("Arquivo não encontrado no storage.")
        }
        val ttl = Duration.ofSeconds(storageProperties.downloadTtlSeconds)
        return objectStoragePort.presignGetUrl(storageKey, ttl, fileName)
    }

    fun ttlSeconds(): Long = storageProperties.downloadTtlSeconds
}
