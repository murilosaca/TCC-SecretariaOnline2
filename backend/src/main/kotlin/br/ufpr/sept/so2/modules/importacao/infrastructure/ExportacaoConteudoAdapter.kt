package br.ufpr.sept.so2.modules.importacao.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.importacao.application.ports.ExportacaoConteudoPort
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ExportacaoConteudoAdapter(
    private val jdbcTemplate: JdbcTemplate,
    private val cursoEscopoPort: CursoEscopoPort,
    private val cursoRepository: CursoRepository,
) : ExportacaoConteudoPort {
    override fun csv(operadorId: UUID, kind: String, filtros: Map<String, String?>): ByteArray {
        val cursos = cursosDo(operadorId, filtros["curso"])
        val sql = SQL.getValue(kind)
        val marcas = cursos.joinToString(",") { "?" }
        val consulta = sql.replace("/*CURSOS*/", marcas)
        val linhas = jdbcTemplate.queryForList(consulta, *cursos.toTypedArray())
        return montar(kind, linhas).toByteArray(Charsets.UTF_8)
    }

    private fun cursosDo(operadorId: UUID, sigla: String?): List<UUID> {
        val vinculados = cursoEscopoPort.cursoIdsDoUsuario(operadorId)
        if (vinculados.isEmpty()) {
            throw AcessoNegadoException("Nenhum curso vinculado à sua secretaria foi encontrado.")
        }
        if (sigla.isNullOrBlank()) {
            return vinculados.toList()
        }
        val curso = cursoRepository.findBySigla(sigla.trim()).orElse(null)
            ?: throw AcessoNegadoException("Curso fora do seu escopo.")
        if (curso.id !in vinculados) {
            throw AcessoNegadoException("Curso fora do seu escopo.")
        }
        return listOf(curso.id)
    }

    private fun montar(kind: String, linhas: List<Map<String, Any?>>): String {
        val colunas = COLUNAS.getValue(kind)
        val sb = StringBuilder()
        sb.append(colunas.joinToString(",")).append('\n')
        linhas.forEach { linha ->
            val normalizada = linha.mapKeys { it.key.lowercase() }
            sb.append(colunas.joinToString(",") { coluna -> csv(normalizada[coluna]) }).append('\n')
        }
        return sb.toString()
    }

    private fun csv(valor: Any?): String {
        val texto = valor?.toString().orEmpty()
        return if (texto.any { it == ',' || it == '"' || it == '\n' }) {
            "\"" + texto.replace("\"", "\"\"") + "\""
        } else {
            texto
        }
    }

    companion object {
        private val ALUNOS_SQL = """
            SELECT a.nome, a.grr, a.email_institucional AS email, c.sigla, a.situacao
            FROM aluno a JOIN curso c ON c.id = a.id_curso
            WHERE a.id_curso IN (/*CURSOS*/)
            ORDER BY a.nome
            """.trimIndent()

        private val SQL: Map<String, String> = mapOf(
            "alunos" to ALUNOS_SQL,
            "solicitacoes" to """
                SELECT s.protocolo, s.tipo_codigo, s.estado, s.created_at
                FROM solicitacao s
                WHERE s.solicitante_id IN (
                    SELECT u.id FROM usuario u
                    JOIN aluno a ON LOWER(CAST(a.email_institucional AS VARCHAR)) = LOWER(CAST(u.email_institucional AS VARCHAR))
                    WHERE a.id_curso IN (/*CURSOS*/)
                )
                ORDER BY s.created_at DESC
                """.trimIndent(),
            "presencas" to """
                SELECT p.evento_id, p.usuario_id, p.fase, p.instante
                FROM presenca p
                WHERE p.usuario_id IN (
                    SELECT u.id FROM usuario u
                    JOIN aluno a ON LOWER(CAST(a.email_institucional AS VARCHAR)) = LOWER(CAST(u.email_institucional AS VARCHAR))
                    WHERE a.id_curso IN (/*CURSOS*/)
                )
                ORDER BY p.instante DESC
                """.trimIndent(),
            "certificados" to """
                SELECT c.titulo, c.hash_sha256, c.emitido_em, a.grr
                FROM certificado c JOIN aluno a ON a.id = c.id_aluno
                WHERE a.id_curso IN (/*CURSOS*/)
                ORDER BY c.emitido_em DESC
                """.trimIndent(),
            "egressos" to """
                SELECT a.nome, c.sigla, d.situacao, d.data_colacao
                FROM diploma d
                JOIN aluno a ON a.id = d.id_aluno
                JOIN curso c ON c.id = d.id_curso
                WHERE d.id_curso IN (/*CURSOS*/)
                ORDER BY a.nome
                """.trimIndent(),
            "formativas" to """
                SELECT f.titulo, f.estado, f.carga_horaria, a.grr
                FROM formativa f JOIN aluno a ON a.id = f.id_aluno
                WHERE a.id_curso IN (/*CURSOS*/)
                ORDER BY f.created_at DESC
                """.trimIndent(),
        )

        private val COLUNAS: Map<String, List<String>> = mapOf(
            "alunos" to listOf("nome", "grr", "email", "sigla", "situacao"),
            "solicitacoes" to listOf("protocolo", "tipo_codigo", "estado", "created_at"),
            "presencas" to listOf("evento_id", "usuario_id", "fase", "instante"),
            "certificados" to listOf("titulo", "hash_sha256", "emitido_em", "grr"),
            "egressos" to listOf("nome", "sigla", "situacao", "data_colacao"),
            "formativas" to listOf("titulo", "estado", "carga_horaria", "grr"),
        )
    }
}
