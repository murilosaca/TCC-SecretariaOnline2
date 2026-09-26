package br.ufpr.sept.so2.modules.reports.infrastructure

import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCoordenadorQueryPort
import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCoordenadorQueryPort.PeriodoFiltro
import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioSecretariaQueryPort
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenadorRegras
import br.ufpr.sept.so2.modules.reports.domain.RelatorioSecretaria
import br.ufpr.sept.so2.modules.reports.domain.RelatorioSecretaria.ItemSolicitacao
import br.ufpr.sept.so2.modules.reports.domain.RelatorioSecretaria.PontoEstado
import br.ufpr.sept.so2.modules.reports.domain.RelatorioSecretaria.PontoHoras
import br.ufpr.sept.so2.modules.reports.domain.RelatorioSecretaria.PontoPresenca
import br.ufpr.sept.so2.modules.reports.domain.RelatorioSecretaria.PontoTipo
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Component
class RelatorioSecretariaQueryAdapter(
    private val jdbc: JdbcTemplate,
    private val periodoPort: RelatorioCoordenadorQueryPort,
) : RelatorioSecretariaQueryPort {

    @Transactional(readOnly = true)
    override fun agregar(cursoIds: Set<UUID>, periodo: PeriodoFiltro?): RelatorioSecretaria {
        if (cursoIds.isEmpty()) {
            return RelatorioSecretaria(
                cursoId = null,
                cursoSigla = null,
                cursoNome = "",
                cursosEscopo = emptyList(),
                periodoCodigo = periodo?.codigo,
                periodoRotulo = periodo?.rotulo,
                solicitacoesPorTipo = emptyList(),
                solicitacoesPorEstado = emptyList(),
                presencas = emptyList(),
                horasFormativas = emptyList(),
                itensSolicitacao = emptyList(),
            )
        }
        val itens = carregarSolicitacoes(cursoIds, periodo)
        val porTipo = itens
            .groupBy { it.tipoCodigo to it.tipoNome }
            .map { (chave, lista) -> PontoTipo(chave.first, chave.second, lista.size) }
            .sortedWith(compareByDescending<PontoTipo> { it.quantidade }.thenBy { it.tipoNome })
        val porEstado = itens
            .groupBy { it.estado }
            .map { (estado, lista) -> PontoEstado(estado, lista.size) }
            .sortedWith(compareByDescending<PontoEstado> { it.quantidade }.thenBy { it.estado })

        val periodos = periodoPort.listarPeriodos().let { todos ->
            if (periodo == null) {
                todos
            } else {
                todos.filter { it.id == periodo.id }
            }
        }

        return RelatorioSecretaria(
            cursoId = null,
            cursoSigla = null,
            cursoNome = "",
            cursosEscopo = emptyList(),
            periodoCodigo = periodo?.codigo,
            periodoRotulo = periodo?.rotulo,
            solicitacoesPorTipo = porTipo,
            solicitacoesPorEstado = porEstado,
            presencas = montarPresencas(cursoIds, periodos),
            horasFormativas = montarHoras(cursoIds, periodos),
            itensSolicitacao = itens,
        )
    }

    private fun carregarSolicitacoes(
        cursoIds: Set<UUID>,
        periodo: PeriodoFiltro?,
    ): List<ItemSolicitacao> {
        val placeholders = cursoIds.joinToString(",") { "?" }
        val inicioTs = toTs(periodo?.inicio?.atStartOfDay()?.atOffset(ZoneOffset.UTC))
        val fimTs = toTs(periodo?.fim?.plusDays(1)?.atStartOfDay()?.atOffset(ZoneOffset.UTC))
        val args = mutableListOf<Any>(*cursoIds.toTypedArray())

        val sql = buildString {
            append(
                """
                SELECT s.id, s.protocolo, s.tipo_codigo, s.tipo_nome, s.estado, s.created_at, c.sigla AS curso_sigla
                FROM solicitacao s
                INNER JOIN usuario u ON u.id = s.solicitante_id
                INNER JOIN aluno a ON (
                  LOWER(CAST(a.email_institucional AS VARCHAR)) = LOWER(CAST(u.email_institucional AS VARCHAR))
                  OR (u.grr IS NOT NULL AND UPPER(a.grr) = UPPER(u.grr))
                )
                INNER JOIN curso c ON c.id = a.id_curso
                WHERE a.id_curso IN ($placeholders)
                """.trimIndent(),
            )
            if (inicioTs != null) {
                append(" AND s.created_at >= ?")
                args += inicioTs
            }
            if (fimTs != null) {
                append(" AND s.created_at < ?")
                args += fimTs
            }
            append(" ORDER BY s.created_at DESC")
        }

        return jdbc.query(sql, { rs, _ ->
            ItemSolicitacao(
                id = rs.getObject("id", UUID::class.java),
                protocolo = rs.getString("protocolo"),
                tipoCodigo = rs.getString("tipo_codigo"),
                tipoNome = rs.getString("tipo_nome"),
                estado = rs.getString("estado"),
                createdAt = rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC),
                cursoSigla = rs.getString("curso_sigla"),
            )
        }, *args.toTypedArray())
    }

    private fun montarPresencas(
        cursoIds: Set<UUID>,
        periodos: List<RelatorioCoordenadorQueryPort.PeriodoOpcao>,
    ): List<PontoPresenca> {
        if (periodos.isEmpty()) {
            return emptyList()
        }
        val placeholders = cursoIds.joinToString(",") { "?" }
        data class PresencaKey(val eventoId: UUID, val usuarioId: UUID, val mode: String)

        val rows = jdbc.query(
            """
            SELECT p.evento_id, p.usuario_id, p.fase, e.attendance_mode, e.inicio_em
            FROM presenca p
            INNER JOIN evento e ON e.id = p.evento_id
            INNER JOIN usuario u ON u.id = p.usuario_id
            INNER JOIN aluno a ON (
              LOWER(CAST(a.email_institucional AS VARCHAR)) = LOWER(CAST(u.email_institucional AS VARCHAR))
              OR (u.grr IS NOT NULL AND UPPER(a.grr) = UPPER(u.grr))
            )
            WHERE a.id_curso IN ($placeholders)
            """.trimIndent(),
            { rs, _ ->
                Triple(
                    PresencaKey(
                        rs.getObject("evento_id", UUID::class.java),
                        rs.getObject("usuario_id", UUID::class.java),
                        rs.getString("attendance_mode"),
                    ),
                    rs.getString("fase"),
                    rs.getTimestamp("inicio_em").toInstant().atOffset(ZoneOffset.UTC),
                )
            },
            *cursoIds.toTypedArray(),
        )

        return periodos.map { p ->
            val inicio = p.inicio.atStartOfDay().atOffset(ZoneOffset.UTC)
            val fim = p.fim.atStartOfDay().atOffset(ZoneOffset.UTC).plusDays(1)
            val noPeriodo = rows.filter { !it.third.isBefore(inicio) && it.third.isBefore(fim) }
            val porAlunoEvento = noPeriodo.groupBy { it.first }
            var confirmadas = 0
            for ((key, fases) in porAlunoEvento) {
                val nomes = fases.map { it.second }.toSet()
                val completa = "ENTRADA" in nomes &&
                    (key.mode in SINGLE_MODES || "SAIDA" in nomes)
                if (completa) {
                    confirmadas++
                }
            }
            PontoPresenca(
                RelatorioCoordenadorRegras.rotulo(p.ano, p.semestre),
                confirmadas,
                porAlunoEvento.size,
            )
        }
    }

    private fun montarHoras(
        cursoIds: Set<UUID>,
        periodos: List<RelatorioCoordenadorQueryPort.PeriodoOpcao>,
    ): List<PontoHoras> {
        if (periodos.isEmpty()) {
            return emptyList()
        }
        val placeholders = cursoIds.joinToString(",") { "?" }
        data class FormRow(val carga: Int, val estado: String, val quando: OffsetDateTime)

        val formativas = jdbc.query(
            """
            SELECT f.carga_horaria, f.estado, f.reviewed_at, f.updated_at
            FROM formativa f
            INNER JOIN aluno a ON a.id = f.id_aluno
            WHERE a.id_curso IN ($placeholders)
            """.trimIndent(),
            { rs, _ ->
                val reviewed = rs.getTimestamp("reviewed_at")
                val updated = rs.getTimestamp("updated_at")
                FormRow(
                    rs.getInt("carga_horaria"),
                    rs.getString("estado"),
                    (reviewed ?: updated).toInstant().atOffset(ZoneOffset.UTC),
                )
            },
            *cursoIds.toTypedArray(),
        )

        return periodos.map { p ->
            val inicio = p.inicio.atStartOfDay().atOffset(ZoneOffset.UTC)
            val fim = p.fim.atStartOfDay().atOffset(ZoneOffset.UTC).plusDays(1)
            val horas = formativas
                .filter { it.estado == "APROVADA" && !it.quando.isBefore(inicio) && it.quando.isBefore(fim) }
                .sumOf { it.carga }
            PontoHoras(RelatorioCoordenadorRegras.rotulo(p.ano, p.semestre), horas)
        }
    }

    private fun toTs(valor: OffsetDateTime?): Timestamp? =
        valor?.let { Timestamp.from(it.toInstant()) }

    companion object {
        private val SINGLE_MODES = setOf("QR_SINGLE", "SECRET_SINGLE")
    }
}
