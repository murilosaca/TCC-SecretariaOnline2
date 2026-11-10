package br.ufpr.sept.so2.modules.reports.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository
import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo
import br.ufpr.sept.so2.modules.academico.infrastructure.AcademicoDevDataLoader
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.application.ports.TipoAtividadeFormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Seed de demo para /coordenacao/relatorios: períodos letivos + formativas
 * aprovadas/indeferidas datadas, para as séries históricas não ficarem vazias.
 */
@Component
@Profile("dev")
@Order(29)
class RelatoriosCoordenacaoDevDataLoader(
    private val properties: IamProperties,
    private val cursoRepository: CursoRepository,
    private val periodoRepository: PeriodoLetivoRepository,
    private val alunoRepository: AlunoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val tipoRepository: TipoAtividadeFormativaRepository,
    private val formativaRepository: FormativaRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (!properties.seed.enabled) {
            return
        }
        val curso = cursoRepository.findByCodigo(AcademicoDevDataLoader.CODIGO_TADS).orElse(null)
        if (curso == null) {
            LOG.warn("Seed de relatórios ignorado: curso TADS ausente.")
            return
        }
        val aluno = alunoRepository.findByGrr(GRR_ALUNO).orElse(null)
        val colega = alunoRepository.findByGrr(GRR_COLEGA).orElse(null)
        if (aluno == null) {
            LOG.warn("Seed de relatórios ignorado: aluno de desenvolvimento ausente.")
            return
        }
        val revisor = usuarioRepository.findByEmail(EMAIL_CAAF).orElseGet {
            usuarioRepository.findByEmail(EMAIL_PROFESSOR).orElse(null)
        }
        if (revisor == null) {
            LOG.warn("Seed de relatórios ignorado: revisor de desenvolvimento ausente.")
            return
        }
        val tipo = tipoRepository.findByCursoENome(curso.id, TIPO_NOME)
        if (tipo == null) {
            LOG.warn("Seed de relatórios ignorado: tipo de atividade ausente.")
            return
        }

        PERIODOS.forEach { def ->
            if (!periodoRepository.existsByAnoAndSemestre(def.ano, def.semestre)) {
                val agora = OffsetDateTime.now()
                periodoRepository.save(
                    PeriodoLetivo(
                        Uuids.v7(),
                        def.ano,
                        def.semestre,
                        def.inicio,
                        def.fim,
                        true,
                        agora,
                        agora,
                    ),
                )
            }
        }

        val jaSemeado = formativaRepository.findByAluno(aluno.id, PageRequest.of(0, 50)).content
            .any { it.titulo.startsWith(TITULO_PREFIXO) }
        if (jaSemeado) {
            LOG.info("Seed de relatórios já presente; períodos garantidos.")
            return
        }

        PERIODOS.forEachIndexed { indice, def ->
            val meio = def.inicio.plusDays(45).atStartOfDay().atOffset(ZoneOffset.UTC)
            val alvo = if (indice % 2 == 0) aluno else (colega ?: aluno)
            val aprovada = Formativa.viaComprovante(
                Uuids.v7(),
                alvo.id,
                tipo.id,
                "$TITULO_PREFIXO ${def.ano}-${def.semestre} aprovada",
                4 + indice * 2,
                "dev/seed-relatorio-${def.ano}-${def.semestre}.pdf",
                meio.minusDays(3),
            )
            aprovada.aprovar("Parecer de desenvolvimento: horas validadas para o relatório.", revisor.id, meio)
            formativaRepository.save(aprovada)

            val indeferida = Formativa.viaComprovante(
                Uuids.v7(),
                aluno.id,
                tipo.id,
                "$TITULO_PREFIXO ${def.ano}-${def.semestre} indeferida",
                2,
                "dev/seed-relatorio-${def.ano}-${def.semestre}-ind.pdf",
                meio.minusDays(2),
            )
            indeferida.indeferir(
                "Parecer de desenvolvimento: comprovante insuficiente para validação neste período.",
                revisor.id,
                meio.plusDays(1),
            )
            formativaRepository.save(indeferida)
        }

        LOG.info(
            "Relatórios de coordenação de desenvolvimento prontos ({} períodos + formativas datadas).",
            PERIODOS.size,
        )
    }

    private data class PeriodoDef(val ano: Int, val semestre: Int, val inicio: LocalDate, val fim: LocalDate)

    companion object {
        private const val GRR_ALUNO = "GRR20240001"
        private const val GRR_COLEGA = "GRR20240002"
        private const val EMAIL_CAAF = "caaf.dev@ufpr.br"
        private const val EMAIL_PROFESSOR = "professor.dev@ufpr.br"
        private const val TIPO_NOME = "Curso de extensão"
        private const val TITULO_PREFIXO = "Seed relatório"
        private val PERIODOS = listOf(
            PeriodoDef(2025, 1, LocalDate.of(2025, 2, 17), LocalDate.of(2025, 6, 28)),
            PeriodoDef(2025, 2, LocalDate.of(2025, 8, 4), LocalDate.of(2025, 12, 13)),
            PeriodoDef(2026, 1, LocalDate.of(2026, 2, 16), LocalDate.of(2026, 6, 27)),
        )
        private val LOG = LoggerFactory.getLogger(RelatoriosCoordenacaoDevDataLoader::class.java)
    }
}
