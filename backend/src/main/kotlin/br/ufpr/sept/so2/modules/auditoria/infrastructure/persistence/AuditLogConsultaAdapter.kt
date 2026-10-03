package br.ufpr.sept.so2.modules.auditoria.infrastructure.persistence

import br.ufpr.sept.so2.modules.auditoria.application.AuditLogItem
import br.ufpr.sept.so2.modules.auditoria.application.ports.AuditLogConsultaPort
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Component
class AuditLogConsultaAdapter(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) : AuditLogConsultaPort {

    override fun contar(ator: String?, acao: String?, de: OffsetDateTime, ateExclusivo: OffsetDateTime): Long {
        val filtro = Filtro(ator, acao, de, ateExclusivo)
        val sql = "SELECT COUNT(*) FROM audit_log a LEFT JOIN usuario u ON u.id = a.ator_id ${filtro.where}"
        return jdbcTemplate.queryForObject(sql, Long::class.java, *filtro.args) ?: 0L
    }

    override fun listar(
        ator: String?,
        acao: String?,
        de: OffsetDateTime,
        ateExclusivo: OffsetDateTime,
        offset: Int,
        limite: Int,
    ): List<AuditLogItem> {
        val filtro = Filtro(ator, acao, de, ateExclusivo)
        val sql = """
            SELECT a.id, a.tipo, a.ator_id, u.nome AS ator_nome, a.ip, a.created_at,
                   a.payload_antes, COALESCE(a.payload_depois, a.payload) AS payload_depois
            FROM audit_log a
            LEFT JOIN usuario u ON u.id = a.ator_id
            ${filtro.where}
            ORDER BY a.created_at DESC
            LIMIT ? OFFSET ?
            """.trimIndent()
        val args = filtro.args.toMutableList()
        args.add(limite)
        args.add(offset)
        return jdbcTemplate.query(sql, { rs, _ -> mapear(rs) }, *args.toTypedArray())
    }

    private fun mapear(rs: ResultSet): AuditLogItem {
        val depois = rs.getString("payload_depois")
        return AuditLogItem(
            id = uuid(rs, "id"),
            atorId = uuidOuNulo(rs, "ator_id"),
            atorNome = rs.getString("ator_nome"),
            acao = rs.getString("tipo"),
            entidade = entidadeDe(depois),
            ip = rs.getString("ip"),
            timestamp = instante(rs, "created_at"),
            payloadAntes = rs.getString("payload_antes"),
            payloadDepois = depois,
        )
    }

    private fun uuid(rs: ResultSet, coluna: String): UUID = uuidOuNulo(rs, coluna)!!

    private fun uuidOuNulo(rs: ResultSet, coluna: String): UUID? {
        val valor = rs.getObject(coluna) ?: return null
        return when (valor) {
            is UUID -> valor
            else -> UUID.fromString(valor.toString())
        }
    }

    private fun instante(rs: ResultSet, coluna: String): OffsetDateTime {
        val valor = rs.getObject(coluna)
        return when (valor) {
            is OffsetDateTime -> valor
            is Timestamp -> valor.toInstant().atOffset(ZoneOffset.UTC)
            else -> OffsetDateTime.parse(valor.toString())
        }
    }

    private fun entidadeDe(payload: String?): String? {
        if (payload.isNullOrBlank() || !payload.trim().startsWith("{")) {
            return null
        }
        return try {
            val node = objectMapper.readTree(payload)
            val entidade = node.path("entidade").asText(null)
            if (!entidade.isNullOrBlank()) {
                entidade
            } else {
                val kind = node.path("kind").asText(null)
                if (kind.isNullOrBlank()) null else kind
            }
        } catch (_: Exception) {
            null
        }
    }

    private class Filtro(
        ator: String?,
        acao: String?,
        de: OffsetDateTime,
        ateExclusivo: OffsetDateTime,
    ) {
        val where: String
        val args: Array<Any>

        init {
            val clausulas = mutableListOf("a.created_at >= ?", "a.created_at < ?")
            val valores = mutableListOf<Any>(de, ateExclusivo)
            if (!ator.isNullOrBlank()) {
                clausulas.add("LOWER(u.nome) LIKE ?")
                valores.add("%${ator.trim().lowercase()}%")
            }
            if (!acao.isNullOrBlank()) {
                clausulas.add("LOWER(a.tipo) = ?")
                valores.add(acao.trim().lowercase())
            }
            where = "WHERE " + clausulas.joinToString(" AND ")
            args = valores.toTypedArray()
        }
    }
}
