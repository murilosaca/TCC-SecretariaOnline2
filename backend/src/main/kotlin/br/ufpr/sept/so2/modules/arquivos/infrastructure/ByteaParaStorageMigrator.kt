package br.ufpr.sept.so2.modules.arquivos.infrastructure

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.arquivos.domain.StorageKey
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Migra PDFs legados em bytea para o storage S3-compatível e grava storage_key.
 * Idempotente: só processa linhas com storage_key nulo e bytes presentes.
 * Em H2 de teste (sem Flyway) a coluna pode não existir — ignora com log.
 */
@Component
class ByteaParaStorageMigrator(
    private val jdbcTemplate: JdbcTemplate,
    private val objectStoragePort: ObjectStoragePort,
) {

    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun migrar() {
        try {
            migrarCertificados()
            migrarEstagioDocumentos()
            migrarTccs()
        } catch (ex: Exception) {
            LOG.warn("Migração bytea→storage ignorada neste ambiente: {}", ex.message)
        }
    }

    private fun migrarCertificados() {
        val rows = jdbcTemplate.query(
            """
            SELECT id, pdf FROM certificado
            WHERE storage_key IS NULL AND pdf IS NOT NULL
            """.trimIndent(),
        ) { rs, _ ->
            UUID.fromString(rs.getString("id")) to rs.getBytes("pdf")
        }
        rows.forEach { (id, pdf) ->
            if (pdf == null || pdf.isEmpty()) {
                return@forEach
            }
            val key = StorageKey.certificado(id).value
            objectStoragePort.putObject(key, "application/pdf", pdf)
            jdbcTemplate.update(
                "UPDATE certificado SET storage_key = ?, pdf = NULL WHERE id = ?",
                key,
                id,
            )
            LOG.info("Certificado {} migrado para {}", id, key)
        }
    }

    private fun migrarEstagioDocumentos() {
        val rows = jdbcTemplate.query(
            """
            SELECT d.id, d.id_estagio, d.conteudo, d.content_type
            FROM estagio_documento d
            WHERE d.storage_key IS NULL AND d.conteudo IS NOT NULL
            """.trimIndent(),
        ) { rs, _ ->
            RowDoc(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("id_estagio")),
                rs.getBytes("conteudo"),
                rs.getString("content_type") ?: "application/pdf",
            )
        }
        rows.forEach { row ->
            val bytes = row.bytes ?: return@forEach
            if (bytes.isEmpty()) {
                return@forEach
            }
            val key = StorageKey.estagioDocumento(row.estagioId, row.id).value
            objectStoragePort.putObject(key, row.contentType, bytes)
            jdbcTemplate.update(
                "UPDATE estagio_documento SET storage_key = ?, conteudo = NULL WHERE id = ?",
                key,
                row.id,
            )
            LOG.info("Documento de estágio {} migrado para {}", row.id, key)
        }
    }

    private fun migrarTccs() {
        val rows = jdbcTemplate.query(
            """
            SELECT id, conteudo, content_type FROM tcc
            WHERE storage_key IS NULL AND conteudo IS NOT NULL
            """.trimIndent(),
        ) { rs, _ ->
            RowTcc(
                UUID.fromString(rs.getString("id")),
                rs.getBytes("conteudo"),
                rs.getString("content_type") ?: "application/pdf",
            )
        }
        rows.forEach { row ->
            val bytes = row.bytes ?: return@forEach
            if (bytes.isEmpty()) {
                return@forEach
            }
            val key = StorageKey.tccVersaoFinal(row.id).value
            objectStoragePort.putObject(key, row.contentType, bytes)
            jdbcTemplate.update(
                "UPDATE tcc SET storage_key = ?, conteudo = NULL WHERE id = ?",
                key,
                row.id,
            )
            LOG.info("TCC {} migrado para {}", row.id, key)
        }
    }

    private data class RowDoc(
        val id: UUID,
        val estagioId: UUID,
        val bytes: ByteArray?,
        val contentType: String,
    )

    private data class RowTcc(
        val id: UUID,
        val bytes: ByteArray?,
        val contentType: String,
    )

    companion object {
        private val LOG = LoggerFactory.getLogger(ByteaParaStorageMigrator::class.java)
    }
}
