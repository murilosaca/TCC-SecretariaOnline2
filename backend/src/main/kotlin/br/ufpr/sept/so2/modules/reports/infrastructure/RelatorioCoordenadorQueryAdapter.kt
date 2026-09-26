package br.ufpr.sept.so2.modules.reports.infrastructure

import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCoordenadorQueryPort
import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCoordenadorQueryPort.PeriodoFiltro
import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCoordenadorQueryPort.PeriodoOpcao
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador.CargaDeliberador
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador.KpisCoordenador
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador.PendenciaCoordenador
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador.PontoAprovacao
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador.PontoEvasao
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador.PontoFormativa
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador.SeriesCoordenador
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenadorRegras
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Component
class RelatorioCoordenadorQueryAdapter(
    private val jdbc: JdbcTemplate,
) : RelatorioCoordenadorQueryPort {

    @Transactional(readOnly = true)
    override fun listarPeriodos(): List<PeriodoOpcao> =
        jdbc.query(
            """
            SELECT id, ano, semestre, inicio, fim
            FROM periodo_letivo
            WHERE ativo = TRUE
            ORDER BY ano ASC, semestre ASC
            """.trimIndent(),
        ) { rs, _ ->
            PeriodoOpcao(
                rs.getObject("id", UUID::class.java),
                rs.getShort("ano").toInt(),
                rs.getShort("semestre").toInt(),
                rs.getDate("inicio").toLocalDate(),
                rs.getDate("fim").toLocalDate(),
            )
        }

    @Transactional(readOnly = true)
    override fun agregar(cursoId: UUID, periodo: PeriodoFiltro?): RelatorioCoordenador {
        val periodos = listarPeriodos()
        return RelatorioCoordenador(
            cursoId = cursoId,
            cursoSigla = "",
            cursoNome = "",
            periodoCodigo = periodo?.codigo,
            periodoRotulo = periodo?.rotulo,
            kpis = montarKpis(cursoId, periodo),
            series = montarSeries(cursoId, periodos),
            pendencias = montarPendencias(cursoId),
            cargaPorDeliberador = montarCarga(cursoId, periodo),
        )
    }

    private fun montarKpis(cursoId: UUID, periodo: PeriodoFiltro?): KpisCoordenador {
        val inicioTs = toTs(periodo?.inicio?.atStartOfDay()?.atOffset(ZoneOffset.UTC))
        val fimTs = toTs(periodo?.fim?.plusDays(1)?.atStartOfDay()?.atOffset(ZoneOffset.UTC))

        data class DelibRow(val estado: String, val createdAt: OffsetDateTime, val deliberadoEm: OffsetDateTime?)

        val delibs = jdbc.query(
            """
            SELECT s.estado, s.created_at,
              (
                SELECT MIN(se.created_at)
                FROM solicitacao_evento se
                WHERE se.solicitacao_id = s.id
                  AND se.tipo IN ('DEFER', 'INDEFER', 'REQUEST_ADJUST')
              ) AS deliberado_em
            FROM solicitacao s
            INNER JOIN usuario u ON u.id = s.solicitante_id
            INNER JOIN aluno a ON (
              LOWER(CAST(a.email_institucional AS VARCHAR)) = LOWER(CAST(u.email_institucional AS VARCHAR))
              OR (u.grr IS NOT NULL AND UPPER(a.grr) = UPPER(u.grr))
            )
            WHERE a.id_curso = ?
            """.trimIndent(),
            { rs, _ ->
                DelibRow(
                    rs.getString("estado"),
                    rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC),
                    rs.getTimestamp("deliberado_em")?.toInstant()?.atOffset(ZoneOffset.UTC),
                )
            },
            cursoId,
        ).filter { row ->
            (inicioTs == null || !row.createdAt.isBefore(inicioTs.toInstant().atOffset(ZoneOffset.UTC))) &&
                (fimTs == null || row.createdAt.isBefore(fimTs.toInstant().atOffset(ZoneOffset.UTC)))
        }

        val decididas = delibs.filter { it.estado in ESTADOS_DECIDIDOS }
        val indeferidas = decididas.count { it.estado == "INDEFERIDA" }
        val tempos = decididas.mapNotNull { row ->
            row.deliberadoEm?.let { fim ->
                java.time.Duration.between(row.createdAt, fim).toMillis().toDouble() / 86_400_000.0
            }
        }
        val taxaIndeferimento = if (decididas.isEmpty()) 0.0 else indeferidas.toDouble() / decididas.size.toDouble()
        val tempoMedio = if (tempos.isEmpty()) 0.0 else tempos.average()

        val formativas = jdbc.query(
            """
            SELECT f.carga_horaria, f.estado, f.reviewed_at, f.updated_at
            FROM formativa f
            INNER JOIN aluno a ON a.id = f.id_aluno
            WHERE a.id_curso = ?
            """.trimIndent(),
            { rs, _ ->
                val reviewed = rs.getTimestamp("reviewed_at")
                val updated = rs.getTimestamp("updated_at")
                val quando = (reviewed ?: updated).toInstant().atOffset(ZoneOffset.UTC)
                Triple(rs.getInt("carga_horaria"), rs.getString("estado"), quando)
            },
            cursoId,
        ).filter { (_, _, quando) ->
            (inicioTs == null || !quando.isBefore(inicioTs.toInstant().atOffset(ZoneOffset.UTC))) &&
                (fimTs == null || quando.isBefore(fimTs.toInstant().atOffset(ZoneOffset.UTC)))
        }
        val horas = formativas.filter { it.second == "APROVADA" }.sumOf { it.first }

        data class PresencaKey(val eventoId: UUID, val usuarioId: UUID, val mode: String)

        val presencas = jdbc.query(
            """
            SELECT p.evento_id, p.usuario_id, p.fase, e.attendance_mode, e.inicio_em
            FROM presenca p
            INNER JOIN evento e ON e.id = p.evento_id
            INNER JOIN usuario u ON u.id = p.usuario_id
            INNER JOIN aluno a ON (
              LOWER(CAST(a.email_institucional AS VARCHAR)) = LOWER(CAST(u.email_institucional AS VARCHAR))
              OR (u.grr IS NOT NULL AND UPPER(a.grr) = UPPER(u.grr))
            )
            WHERE a.id_curso = ?
            """.trimIndent(),
            { rs, _ ->
                val inicioEm = rs.getTimestamp("inicio_em").toInstant().atOffset(ZoneOffset.UTC)
                Triple(
                    PresencaKey(
                        rs.getObject("evento_id", UUID::class.java),
                        rs.getObject("usuario_id", UUID::class.java),
                        rs.getString("attendance_mode"),
                    ),
                    rs.getString("fase"),
                    inicioEm,
                )
            },
            cursoId,
        ).filter { (_, _, inicioEm) ->
            (inicioTs == null || !inicioEm.isBefore(inicioTs.toInstant().atOffset(ZoneOffset.UTC))) &&
                (fimTs == null || inicioEm.isBefore(fimTs.toInstant().atOffset(ZoneOffset.UTC)))
        }

        val porAlunoEvento = presencas.groupBy { it.first }
        var completas = 0
        for ((key, fases) in porAlunoEvento) {
            val nomes = fases.map { it.second }.toSet()
            val completa = "ENTRADA" in nomes &&
                (key.mode in SINGLE_MODES || "SAIDA" in nomes)
            if (completa) {
                completas++
            }
        }
        val taxaPresenca = if (porAlunoEvento.isEmpty()) {
            0.0
        } else {
            completas.toDouble() / porAlunoEvento.size.toDouble()
        }

        return KpisCoordenador(
            tempoMedioDias = arredondar(tempoMedio),
            taxaIndeferimento = arredondar(taxaIndeferimento),
            horasValidadas = horas,
            taxaPresenca = arredondar(taxaPresenca),
            thresholdIndeferimento = RelatorioCoordenador.THRESHOLD_INDEFERIMENTO_PADRAO,
        )
    }

    private fun montarSeries(
        cursoId: UUID,
        periodos: List<PeriodoOpcao>,
    ): SeriesCoordenador {
        if (periodos.isEmpty()) {
            return SeriesCoordenador(emptyList(), emptyList(), emptyList())
        }

        data class AlunoRow(val situacao: String, val createdAt: OffsetDateTime, val updatedAt: OffsetDateTime)

        val alunos = jdbc.query(
            """
            SELECT situacao, created_at, updated_at FROM aluno WHERE id_curso = ?
            """.trimIndent(),
            { rs, _ ->
                AlunoRow(
                    rs.getString("situacao"),
                    rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC),
                    rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC),
                )
            },
            cursoId,
        )

        data class FormRow(val carga: Int, val estado: String, val quando: OffsetDateTime)

        val formativas = jdbc.query(
            """
            SELECT f.carga_horaria, f.estado, f.reviewed_at, f.updated_at
            FROM formativa f
            INNER JOIN aluno a ON a.id = f.id_aluno
            WHERE a.id_curso = ?
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
            cursoId,
        )

        val evasao = periodos.map { p ->
            val fim = p.fim.atStartOfDay().atOffset(ZoneOffset.UTC).plusDays(1)
            val inicio = p.inicio.atStartOfDay().atOffset(ZoneOffset.UTC)
            val ativos = alunos.count { a ->
                a.createdAt.isBefore(fim) &&
                    (a.situacao in ATIVOS || !a.updatedAt.isBefore(fim))
            }
            val evadidos = alunos.count { a ->
                a.situacao == "DESLIGADO" &&
                    !a.updatedAt.isBefore(inicio) &&
                    a.updatedAt.isBefore(fim)
            }
            PontoEvasao(RelatorioCoordenadorRegras.rotulo(p.ano, p.semestre), ativos, evadidos)
        }

        val formSeries = periodos.map { p ->
            val inicio = p.inicio.atStartOfDay().atOffset(ZoneOffset.UTC)
            val fim = p.fim.atStartOfDay().atOffset(ZoneOffset.UTC).plusDays(1)
            val noPeriodo = formativas.filter { !it.quando.isBefore(inicio) && it.quando.isBefore(fim) }
            val aprovadas = noPeriodo.filter { it.estado == "APROVADA" }
            val indeferidas = noPeriodo.count { it.estado == "INDEFERIDA" }
            PontoFormativa(
                RelatorioCoordenadorRegras.rotulo(p.ano, p.semestre),
                aprovadas.sumOf { it.carga },
                aprovadas.size,
                indeferidas,
            )
        }

        val aprovacao = formSeries.map { ponto ->
            val total = ponto.aprovadas + ponto.indeferidas
            val taxa = if (total == 0) 0.0 else ponto.aprovadas.toDouble() / total.toDouble()
            PontoAprovacao(ponto.periodo, arredondar(taxa))
        }

        return SeriesCoordenador(evasao, formSeries, aprovacao)
    }

    private fun montarPendencias(cursoId: UUID): List<PendenciaCoordenador> {
        val exigidos = jdbc.query(
            """
            SELECT cc.banca_membros_externos
            FROM curso_configuracao cc
            WHERE cc.id = ?
            """.trimIndent(),
            { rs, _ -> rs.getInt(1) },
            cursoId,
        ).firstOrNull() ?: 1

        return jdbc.query(
            """
            SELECT t.id, t.titulo,
              (SELECT COUNT(*) FROM tcc_membro m WHERE m.id_tcc = t.id AND m.papel = 'BANCA') AS banca_count
            FROM tcc t
            WHERE t.id_curso = ?
              AND t.situacao = 'ATIVO'
            ORDER BY t.updated_at DESC
            """.trimIndent(),
            { rs, _ ->
                val bancaCount = rs.getInt("banca_count")
                if (bancaCount >= exigidos) {
                    null
                } else {
                    val id = rs.getObject("id", UUID::class.java)
                    PendenciaCoordenador(
                        id,
                        "Banca sem composição - ${rs.getString("titulo")}",
                        "/tccs/$id",
                    )
                }
            },
            cursoId,
        ).filterNotNull()
    }

    private fun montarCarga(cursoId: UUID, periodo: PeriodoFiltro?): List<CargaDeliberador> {
        val inicioTs = toTs(periodo?.inicio?.atStartOfDay()?.atOffset(ZoneOffset.UTC))
        val fimTs = toTs(periodo?.fim?.plusDays(1)?.atStartOfDay()?.atOffset(ZoneOffset.UTC))

        data class CargaRow(
            val nome: String,
            val createdSolic: OffsetDateTime,
            val createdEvento: OffsetDateTime,
        )

        val rows = jdbc.query(
            """
            SELECT
              COALESCE(NULLIF(TRIM(u.nome), ''), CAST(u.email_institucional AS VARCHAR)) AS nome,
              s.created_at AS solic_em,
              se.created_at AS evento_em
            FROM solicitacao_evento se
            INNER JOIN solicitacao s ON s.id = se.solicitacao_id
            INNER JOIN usuario u ON u.id = se.ator_id
            INNER JOIN usuario solicitante ON solicitante.id = s.solicitante_id
            INNER JOIN aluno a ON (
              LOWER(CAST(a.email_institucional AS VARCHAR)) = LOWER(CAST(solicitante.email_institucional AS VARCHAR))
              OR (solicitante.grr IS NOT NULL AND UPPER(a.grr) = UPPER(solicitante.grr))
            )
            WHERE a.id_curso = ?
              AND se.tipo IN ('DEFER', 'INDEFER', 'REQUEST_ADJUST')
              AND se.ator_id IS NOT NULL
            """.trimIndent(),
            { rs, _ ->
                CargaRow(
                    rs.getString("nome"),
                    rs.getTimestamp("solic_em").toInstant().atOffset(ZoneOffset.UTC),
                    rs.getTimestamp("evento_em").toInstant().atOffset(ZoneOffset.UTC),
                )
            },
            cursoId,
        ).filter { row ->
            (inicioTs == null || !row.createdEvento.isBefore(inicioTs.toInstant().atOffset(ZoneOffset.UTC))) &&
                (fimTs == null || row.createdEvento.isBefore(fimTs.toInstant().atOffset(ZoneOffset.UTC)))
        }

        return rows.groupBy { it.nome }
            .map { (nome, lista) ->
                val tempos = lista.map {
                    java.time.Duration.between(it.createdSolic, it.createdEvento).toMillis().toDouble() / 86_400_000.0
                }
                CargaDeliberador(nome, lista.size, arredondar(tempos.average()))
            }
            .sortedWith(compareByDescending<CargaDeliberador> { it.quantidade }.thenBy { it.nome })
    }

    private fun toTs(valor: OffsetDateTime?): Timestamp? =
        valor?.let { Timestamp.from(it.toInstant()) }

    private fun arredondar(valor: Double): Double =
        if (valor.isNaN()) {
            0.0
        } else {
            BigDecimal.valueOf(valor).setScale(4, RoundingMode.HALF_UP).toDouble()
        }

    companion object {
        private val ESTADOS_DECIDIDOS = setOf("INDEFERIDA", "DELIBERADA", "CONCLUIDA", "EM_AJUSTE")
        private val ATIVOS = setOf("MATRICULADO", "FORMANDO", "TRANCADO")
        private val SINGLE_MODES = setOf("QR_SINGLE", "SECRET_SINGLE")
    }
}
