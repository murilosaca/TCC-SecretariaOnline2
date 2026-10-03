package br.ufpr.sept.so2.modules.busca.infrastructure

import br.ufpr.sept.so2.modules.busca.application.BuscaConsultaPort
import br.ufpr.sept.so2.modules.busca.application.BuscaRegistro
import br.ufpr.sept.so2.modules.busca.application.IdentidadeBusca
import br.ufpr.sept.so2.modules.busca.application.IdentidadeBuscaPort
import br.ufpr.sept.so2.modules.busca.application.SolicitantesNoEscopoPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.SolicitacaoCursoEscopo
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.util.UUID

@Component
class BuscaJdbcAdapter(
    private val jdbc: JdbcTemplate,
) : BuscaConsultaPort {
    override fun alunosPorCursos(termo: String, cursos: Set<UUID>): List<BuscaRegistro> {
        val (filtro, args) = inClause("id_curso", cursos)
        return consultar(
            """
            SELECT id, nome AS titulo, grr AS subtitulo
            FROM aluno
            WHERE (LOWER(nome) LIKE ? OR LOWER(grr) LIKE ?)
              AND $filtro
            ORDER BY nome
            LIMIT $LIMITE
            """.trimIndent(),
            listOf(like(termo), like(termo)) + args,
        )
    }

    override fun alunoProprio(termo: String, grr: String?, email: String): List<BuscaRegistro> =
        consultar(
            """
            SELECT id, nome AS titulo, grr AS subtitulo
            FROM aluno
            WHERE (LOWER(nome) LIKE ? OR LOWER(grr) LIKE ?)
              AND (grr = ? OR LOWER(email_institucional) = LOWER(?))
            ORDER BY nome
            LIMIT $LIMITE
            """.trimIndent(),
            listOf(like(termo), like(termo), grr ?: "", email),
        )

    override fun solicitacoes(termo: String, solicitantes: Set<UUID>): List<BuscaRegistro> {
        val (filtro, args) = inClause("solicitante_id", solicitantes)
        return consultar(
            """
            SELECT id, protocolo AS titulo, tipo_nome AS subtitulo
            FROM solicitacao
            WHERE (LOWER(protocolo) LIKE ? OR LOWER(tipo_codigo) LIKE ? OR LOWER(tipo_nome) LIKE ?)
              AND $filtro
            ORDER BY protocolo
            LIMIT $LIMITE
            """.trimIndent(),
            listOf(like(termo), like(termo), like(termo)) + args,
        )
    }

    override fun eventosPorCursos(termo: String, cursos: Set<UUID>): List<BuscaRegistro> {
        val (filtro, args) = inClause("id_curso", cursos)
        return consultar(
            """
            SELECT id, titulo, estado AS subtitulo
            FROM evento
            WHERE LOWER(titulo) LIKE ?
              AND $filtro
            ORDER BY titulo
            LIMIT $LIMITE
            """.trimIndent(),
            listOf(like(termo)) + args,
        )
    }

    override fun eventosDoAnfitriao(termo: String, anfitriaoId: UUID): List<BuscaRegistro> =
        consultar(
            """
            SELECT id, titulo, estado AS subtitulo
            FROM evento
            WHERE LOWER(titulo) LIKE ?
              AND id_anfitriao = ?
            ORDER BY titulo
            LIMIT $LIMITE
            """.trimIndent(),
            listOf(like(termo), anfitriaoId),
        )

    override fun eventosAbertos(termo: String): List<BuscaRegistro> =
        consultar(
            """
            SELECT id, titulo, estado AS subtitulo
            FROM evento
            WHERE LOWER(titulo) LIKE ?
              AND estado = 'EM_ANDAMENTO'
            ORDER BY titulo
            LIMIT $LIMITE
            """.trimIndent(),
            listOf(like(termo)),
        )

    override fun usuarios(termo: String): List<BuscaRegistro> =
        consultar(
            """
            SELECT id,
                   COALESCE(nome, email_institucional) AS titulo,
                   email_institucional AS subtitulo
            FROM usuario
            WHERE LOWER(COALESCE(nome, '')) LIKE ?
               OR LOWER(email_institucional) LIKE ?
               OR LOWER(COALESCE(grr, '')) LIKE ?
            ORDER BY titulo
            LIMIT $LIMITE
            """.trimIndent(),
            listOf(like(termo), like(termo), like(termo)),
        )

    private fun consultar(sql: String, args: List<Any>): List<BuscaRegistro> =
        jdbc.query(sql, MAPEADOR, *args.toTypedArray())

    private fun like(termo: String) = "%$termo%"

    private fun inClause(coluna: String, ids: Collection<UUID>): Pair<String, List<UUID>> {
        val marcas = ids.joinToString(",") { "?" }
        return "$coluna IN ($marcas)" to ids.toList()
    }

    companion object {
        private const val LIMITE = 8
        private val MAPEADOR = RowMapper { rs: ResultSet, _: Int ->
            BuscaRegistro(uuid(rs), rs.getString("titulo") ?: "", rs.getString("subtitulo") ?: "")
        }

        private fun uuid(rs: ResultSet): UUID {
            val valor = rs.getObject("id")
            return if (valor is UUID) valor else UUID.fromString(valor.toString())
        }
    }
}

@Component
class IdentidadeBuscaAdapter(
    private val usuarioRepository: UsuarioRepository,
) : IdentidadeBuscaPort {
    override fun de(atorId: UUID): IdentidadeBusca? {
        val usuario = usuarioRepository.findById(atorId).orElse(null) ?: return null
        return IdentidadeBusca(usuario.grr?.value, usuario.emailInstitucional.value)
    }
}

@Component
class SolicitantesNoEscopoAdapter(
    private val escopo: SolicitacaoCursoEscopo,
) : SolicitantesNoEscopoPort {
    override fun ids(atorId: UUID): Set<UUID> = escopo.solicitanteIdsNoEscopo(atorId)
}
