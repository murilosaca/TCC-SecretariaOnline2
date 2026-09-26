package br.ufpr.sept.so2.modules.bff.application

import br.ufpr.sept.so2.modules.bff.application.ports.DeliberacaoDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.EstagiosProfessorDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.EventosProfessorDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.FormativasCaafDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.ProfessorIdentidadeQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.TccsProfessorDashboardQueryPort
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.Executor

class ProfessorDashboardApplicationServiceTest : StringSpec({
    val professorId = UUID.fromString("01800000-0000-7000-8000-0000000000a1")
    val eventoId = UUID.fromString("01800000-0000-7000-8000-0000000000a2")
    val sync: Executor = Executor { it.run() }

    "bloco nulo não derruba agregação e CAAF só com formative.review" {
        val service = ProfessorDashboardApplicationService(
            ProfessorIdentidadeQueryPort {
                ProfessorIdentidadeQueryPort.ProfessorIdentidade("Prof Dev")
            },
            DeliberacaoDashboardQueryPort { error("solicitações indisponível") },
            EventosProfessorDashboardQueryPort { _, _ -> error("presença indisponível") },
            FormativasCaafDashboardQueryPort { error("não deveria chamar") },
            EstagiosProfessorDashboardQueryPort { error("estágio indisponível") },
            TccsProfessorDashboardQueryPort { error("tcc indisponível") },
            sync,
        )

        val response = service.execute(
            professorId,
            listOf(
                "dashboard.view_self_professor",
                "request.deliberate",
                "event.manage",
                "internship.review",
                "tcc.review",
            ),
        )

        response.saudacao.nome shouldBe "Prof Dev"
        response.kpis.pendentesDeliberar.shouldBeNull()
        response.kpis.slaUrgentes.shouldBeNull()
        response.kpis.eventosHoje.shouldBeNull()
        response.kpis.formativasRevisao.shouldBeNull()
        response.filaSolicitacoes.shouldBeNull()
        response.meusEventos.shouldBeNull()
        response.formativasCaaf.shouldBeNull()
        response.estagiosPendentes.shouldBeNull()
        response.tccsPendentes.shouldBeNull()
        response.links["self"] shouldBe "/bff/dashboard/professor"
        response.links["deliberar"] shouldBe "/solicitacoes?to=me"
        response.links["eventos"] shouldBe "/professor/eventos"
        response.links["formativasCaaf"].shouldBeNull()
        response.links["estagios"] shouldBe "/estagios?to=me"
        response.links["tccs"] shouldBe "/tccs?to=me"
    }

    "com formative.review agrega CAAF e lista vazia não é degradação" {
        val formativaId = UUID.fromString("01800000-0000-7000-8000-0000000000a3")
        val inicio = OffsetDateTime.parse("2026-09-26T19:00:00Z")
        val service = ProfessorDashboardApplicationService(
            ProfessorIdentidadeQueryPort {
                ProfessorIdentidadeQueryPort.ProfessorIdentidade("Prof CAAF")
            },
            DeliberacaoDashboardQueryPort {
                DeliberacaoDashboardQueryPort.DeliberacaoDashboard(0, 0, emptyList())
            },
            EventosProfessorDashboardQueryPort { _, _ ->
                EventosProfessorDashboardQueryPort.EventosProfessorDashboard(
                    1,
                    listOf(
                        EventosProfessorDashboardQueryPort.MeuEventoResumo(
                            eventoId,
                            "Oficina",
                            inicio,
                            inicio.plusHours(2),
                            "EM_ANDAMENTO",
                            mapOf(
                                "self" to "/professor/eventos/$eventoId",
                                "operar" to "/professor/eventos/$eventoId/operacao",
                            ),
                        ),
                    ),
                )
            },
            FormativasCaafDashboardQueryPort {
                FormativasCaafDashboardQueryPort.FormativasCaafDashboard(
                    1,
                    listOf(
                        FormativasCaafDashboardQueryPort.ItemResumo(
                            formativaId,
                            "Horas formativas",
                            "AGUARDANDO_CAAF",
                            "/formativas/$formativaId",
                        ),
                    ),
                )
            },
            EstagiosProfessorDashboardQueryPort {
                EstagiosProfessorDashboardQueryPort.EstagiosProfessorDashboard(0, emptyList())
            },
            TccsProfessorDashboardQueryPort {
                TccsProfessorDashboardQueryPort.TccsProfessorDashboard(0, emptyList())
            },
            sync,
        )

        val response = service.execute(
            professorId,
            listOf(
                "dashboard.view_self_professor",
                "formative.review",
                "internship.review",
                "tcc.review",
                "event.host",
            ),
        )

        response.kpis.pendentesDeliberar shouldBe 0
        response.kpis.slaUrgentes shouldBe 0
        response.kpis.formativasRevisao shouldBe 1
        response.kpis.eventosHoje shouldBe 1
        response.filaSolicitacoes!!.isEmpty() shouldBe true
        response.formativasCaaf!!.size shouldBe 1
        response.formativasCaaf!!.first().titulo shouldBe "Horas formativas"
        response.estagiosPendentes!!.isEmpty() shouldBe true
        response.tccsPendentes!!.isEmpty() shouldBe true
        response.meusEventos!!.first().estado shouldBe "EM_ANDAMENTO"
        response.meusEventos!!.first().links["operar"] shouldBe "/professor/eventos/$eventoId/operacao"
        response.links["formativasCaaf"] shouldBe "/formativas?to=me"
    }
})
