package br.ufpr.sept.so2.modules.arquivos.infrastructure

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URI
import java.time.Duration
import jakarta.annotation.PreDestroy

@Component
@ConditionalOnProperty(prefix = "app.storage", name = ["mode"], havingValue = "s3", matchIfMissing = true)
class S3ObjectStorageAdapter(
    private val properties: StorageProperties,
) : ObjectStoragePort {

    private val client: S3Client = buildClient(properties.endpoint)
    private val presigner: S3Presigner = buildPresigner(properties.publicEndpoint)

    override fun putObject(key: String, contentType: String, bytes: ByteArray) {
        try {
            client.putObject(
                PutObjectRequest.builder()
                    .bucket(properties.bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(bytes.size.toLong())
                    .build(),
                RequestBody.fromBytes(bytes),
            )
        } catch (ex: Exception) {
            LOG.error("Falha ao gravar objeto {} no storage", key, ex)
            throw DadoInvalidoException("Não foi possível gravar o arquivo no storage.")
        }
    }

    override fun getObject(key: String): ByteArray? =
        try {
            client.getObjectAsBytes(
                GetObjectRequest.builder()
                    .bucket(properties.bucket)
                    .key(key)
                    .build(),
            ).asByteArray()
        } catch (_: NoSuchKeyException) {
            null
        } catch (ex: Exception) {
            LOG.error("Falha ao ler objeto {} do storage", key, ex)
            throw RecursoNaoEncontradoException("Arquivo não encontrado no storage.")
        }

    override fun presignGetUrl(key: String, ttl: Duration, fileName: String?): String {
        val get = GetObjectRequest.builder()
            .bucket(properties.bucket)
            .key(key)
            .apply {
                if (!fileName.isNullOrBlank()) {
                    responseContentDisposition("attachment; filename=\"${sanitize(fileName)}\"")
                }
            }
            .build()
        val request = GetObjectPresignRequest.builder()
            .signatureDuration(ttl)
            .getObjectRequest(get)
            .build()
        return presigner.presignGetObject(request).url().toExternalForm()
    }

    override fun presignPutUrl(key: String, contentType: String, ttl: Duration): String {
        val put = PutObjectRequest.builder()
            .bucket(properties.bucket)
            .key(key)
            .contentType(contentType)
            .build()
        val request = PutObjectPresignRequest.builder()
            .signatureDuration(ttl)
            .putObjectRequest(put)
            .build()
        return presigner.presignPutObject(request).url().toExternalForm()
    }

    override fun exists(key: String): Boolean =
        try {
            client.headObject(
                HeadObjectRequest.builder()
                    .bucket(properties.bucket)
                    .key(key)
                    .build(),
            )
            true
        } catch (_: NoSuchKeyException) {
            false
        } catch (ex: Exception) {
            LOG.debug("HeadObject falhou para {}: {}", key, ex.message)
            false
        }

    @PreDestroy
    fun close() {
        client.close()
        presigner.close()
    }

    private fun buildClient(endpoint: String): S3Client {
        val credentials = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(properties.accessKey, properties.secretKey),
        )
        return S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(properties.region))
            .credentialsProvider(credentials)
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(properties.pathStyle)
                    .build(),
            )
            .build()
    }

    private fun buildPresigner(endpoint: String): S3Presigner {
        val credentials = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(properties.accessKey, properties.secretKey),
        )
        return S3Presigner.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(properties.region))
            .credentialsProvider(credentials)
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(properties.pathStyle)
                    .build(),
            )
            .build()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(S3ObjectStorageAdapter::class.java)

        private fun sanitize(name: String): String =
            name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifEmpty { "arquivo.pdf" }
    }
}
