package br.ufpr.sept.so2.modules.bff.application

import br.ufpr.sept.so2.modules.bff.application.ports.AlunoIdentidadeQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.FormativasDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.PeriodoVigenteQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.SolicitacoesDashboardQueryPort
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.Executor

class AlunoDashboardApplicationServiceTest : StringSpec({
    val alunoId = UUID.fromString("01800000-0000-7000-8000-0000000000aa")
    val eventoId = UUID.fromString("01800000-0000-7000-8000-0000000000ee")
    val sync: Executor = Executor { it.run() }

    "bloco opcional nulo não derruba agregação" {
        val service = AlunoDashboardApplicationService(
            AlunoIdentidadeQueryPort { AlunoIdentidadeQueryPort.AlunoIdentidade("Aluno Dev", "TADS") },
            PeriodoVigenteQueryPort {
                PeriodoVigenteQueryPort.PeriodoVigenteResumo(
                    UUID.fromString("01800000-0000-7000-8000-0000000000bb"),
                    2026,
                    2,
                    LocalDate.of(2026, 8, 1),
                    LocalDate.of(2026, 12, 20),
                )
            },
            SolicitacoesDashboardQueryPort { error("solicitações indisponível") },
            EventosDashboardQueryPort { error("presença indisponível") },
            FormativasDashboardQueryPort { error("formativas indisponível") },
            sync,
        )

        val response = service.execute(alunoId, listOf("dashboard.view_own", "request.open"))

        response.saudacao.nome shouldBe "Aluno Dev"
        response.saudacao.curso shouldBe "TADS"
        response.periodoVigente?.rotulo shouldBe "2026/2"
        response.alertaPeriodoAusente shouldBe false
        response.kpis.horasFormativas.shouldBeNull()
        response.kpis.eventosHoje.shouldBeNull()
        response.kpis.certificados.shouldBeNull()
        response.kpis.solicitacoesAbertas.shouldBeNull()
        response.pendencias.shouldBeNull()
        response.pendenciasFormativas.shouldBeNull()
        response.ultimasSolicitacoes.shouldBeNull()
        response.proximosEventos.shouldBeNull()
        response.links["self"] shouldBe "/bff/dashboard/aluno"
        response.links["novaSolicitacao"] shouldBe "/solicitacoes/nova"
    }

    "período ausente gera alerta e lista vazia não é degradação" {
        val solicitacaoId = UUID.fromString("01800000-0000-7000-8000-0000000000cc")
        val service = AlunoDashboardApplicationService(
            AlunoIdentidadeQueryPort { AlunoIdentidadeQueryPort.AlunoIdentidade("Aluno Dev", null) },
            PeriodoVigenteQueryPort { null },
            SolicitacoesDashboardQueryPort {
                SolicitacoesDashboardQueryPort.SolicitacoesDashboard(
                    1,
                    emptyList(),
                    listOf(
                        SolicitacoesDashboardQueryPort.SolicitacaoResumo(
                            solicitacaoId,
                            "PROT-2026-00001",
                            "Declaração simples",
                            "EM_ANALISE",
                            OffsetDateTime.parse("2026-09-20T00:00:00Z"),
                            false,
                        ),
                    ),
                )
            },
            EventosDashboardQueryPort { EventosDashboardQueryPort.EventosDashboard(0, emptyList()) },
            FormativasDashboardQueryPort {
                FormativasDashboardQueryPort.FormativasDashboard(0, 120, emptyList())
            },
            sync,
        )

        val response = service.execute(alunoId, listOf("dashboard.view_own"))

        response.periodoVigente.shouldBeNull()
        response.alertaPeriodoAusente shouldBe true
        response.kpis.solicitacoesAbertas shouldBe 1
        response.kpis.eventosHoje shouldBe 0
        response.kpis.horasFormativas?.validadas shouldBe 0
        response.kpis.horasFormativas?.requeridas shouldBe 120
        response.pendencias.shouldNotBe(null)
        response.pendencias!!.isEmpty() shouldBe true
        response.pendenciasFormativas!!.isEmpty() shouldBe true
        response.proximosEventos!!.isEmpty() shouldBe true
        response.ultimasSolicitacoes!!.first().protocolo shouldBe "PROT-2026-00001"
        response.links["novaSolicitacao"].shouldBeNull()
    }

    "eventosHoje e próximos quando porta responde" {
        val inicio = OffsetDateTime.parse("2026-09-06T19:00:00Z")
        val service = AlunoDashboardApplicationService(
            AlunoIdentidadeQueryPort { AlunoIdentidadeQueryPort.AlunoIdentidade("Aluno Dev", "TADS") },
            PeriodoVigenteQueryPort { null },
            SolicitacoesDashboardQueryPort {
                SolicitacoesDashboardQueryPort.SolicitacoesDashboard(0, emptyList(), emptyList())
            },
            EventosDashboardQueryPort {
                EventosDashboardQueryPort.EventosDashboard(
                    2,
                    listOf(
                        EventosDashboardQueryPort.EventoResumo(
                            eventoId,
                            "Oficina Proof of Stay",
                            inicio,
                            inicio.plusHours(2),
                            true,
                        ),
                    ),
                )
            },
            FormativasDashboardQueryPort {
                FormativasDashboardQueryPort.FormativasDashboard(0, 120, emptyList())
            },
            sync,
        )

        val response = service.execute(alunoId, listOf("dashboard.view_own", "request.open"))

        response.kpis.eventosHoje shouldBe 2
        response.proximosEventos!!.size shouldBe 1
        response.proximosEventos!!.first().titulo shouldBe "Oficina Proof of Stay"
        response.proximosEventos!!.first().href shouldBe "/eventos/$eventoId/presenca"
        response.proximosEventos!!.first().janelaAtiva shouldBe true
        response.kpis.horasFormativas?.validadas shouldBe 0
        response.kpis.certificados.shouldBeNull()
        response.links["novaFormativa"].shouldBeNull()
    }
})
