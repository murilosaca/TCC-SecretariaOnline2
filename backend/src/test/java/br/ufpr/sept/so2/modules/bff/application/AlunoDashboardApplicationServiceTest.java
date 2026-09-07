package br.ufpr.sept.so2.modules.bff.application;

import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse;
import br.ufpr.sept.so2.modules.bff.application.ports.AlunoIdentidadeQueryPort;
import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort;
import br.ufpr.sept.so2.modules.bff.application.ports.PeriodoVigenteQueryPort;
import br.ufpr.sept.so2.modules.bff.application.ports.SolicitacoesDashboardQueryPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlunoDashboardApplicationServiceTest {

    private static final UUID ALUNO_ID = UUID.fromString("01800000-0000-7000-8000-0000000000aa");
    private static final UUID EVENTO_ID = UUID.fromString("01800000-0000-7000-8000-0000000000ee");

    @Test
    void blocoOpcionalNuloNaoDerrubaAgregacao() {
        AlunoDashboardApplicationService service = new AlunoDashboardApplicationService(
                id -> new AlunoIdentidadeQueryPort.AlunoIdentidade("Aluno Dev", "TADS"),
                data -> Optional.of(new PeriodoVigenteQueryPort.PeriodoVigenteResumo(
                        UUID.fromString("01800000-0000-7000-8000-0000000000bb"),
                        2026,
                        2,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 12, 20)
                )),
                id -> {
                    throw new IllegalStateException("solicitações indisponível");
                },
                agora -> {
                    throw new IllegalStateException("presença indisponível");
                },
                Runnable::run
        );

        AlunoDashboardResponse response = service.execute(
                ALUNO_ID,
                List.of("dashboard.view_own", "request.open")
        );

        assertEquals("Aluno Dev", response.saudacao().nome());
        assertEquals("TADS", response.saudacao().curso());
        assertEquals("2026/2", response.periodoVigente().rotulo());
        assertEquals(Boolean.FALSE, response.alertaPeriodoAusente());
        assertNull(response.kpis().horasFormativas());
        assertNull(response.kpis().eventosHoje());
        assertNull(response.kpis().certificados());
        assertNull(response.kpis().solicitacoesAbertas());
        assertNull(response.pendencias());
        assertNull(response.ultimasSolicitacoes());
        assertNull(response.proximosEventos());
        assertEquals("/bff/dashboard/aluno", response.links().get("self"));
        assertEquals("/solicitacoes/nova", response.links().get("novaSolicitacao"));
    }

    @Test
    void periodoAusenteGeraAlertaEListaVaziaNaoEDegradacao() {
        UUID solicitacaoId = UUID.fromString("01800000-0000-7000-8000-0000000000cc");
        AlunoDashboardApplicationService service = new AlunoDashboardApplicationService(
                id -> new AlunoIdentidadeQueryPort.AlunoIdentidade("Aluno Dev", null),
                data -> Optional.empty(),
                id -> new SolicitacoesDashboardQueryPort.SolicitacoesDashboard(
                        1,
                        List.of(),
                        List.of(new SolicitacoesDashboardQueryPort.SolicitacaoResumo(
                                solicitacaoId,
                                "PROT-2026-00001",
                                "Declaração simples",
                                "EM_ANALISE",
                                OffsetDateTime.parse("2026-09-20T00:00:00Z"),
                                false
                        ))
                ),
                agora -> new EventosDashboardQueryPort.EventosDashboard(0, List.of()),
                Runnable::run
        );

        AlunoDashboardResponse response = service.execute(ALUNO_ID, List.of("dashboard.view_own"));

        assertNull(response.periodoVigente());
        assertEquals(Boolean.TRUE, response.alertaPeriodoAusente());
        assertEquals(1, response.kpis().solicitacoesAbertas());
        assertEquals(0, response.kpis().eventosHoje());
        assertTrue(response.pendencias().isEmpty());
        assertTrue(response.proximosEventos().isEmpty());
        assertEquals("PROT-2026-00001", response.ultimasSolicitacoes().getFirst().protocolo());
        assertNull(response.links().get("novaSolicitacao"));
    }

    @Test
    void eventosHojeEProximosQuandoPortaResponde() {
        OffsetDateTime inicio = OffsetDateTime.parse("2026-09-06T19:00:00Z");
        AlunoDashboardApplicationService service = new AlunoDashboardApplicationService(
                id -> new AlunoIdentidadeQueryPort.AlunoIdentidade("Aluno Dev", "TADS"),
                data -> Optional.empty(),
                id -> new SolicitacoesDashboardQueryPort.SolicitacoesDashboard(0, List.of(), List.of()),
                agora -> new EventosDashboardQueryPort.EventosDashboard(
                        2,
                        List.of(new EventosDashboardQueryPort.EventoResumo(
                                EVENTO_ID,
                                "Oficina Proof of Stay",
                                inicio,
                                inicio.plusHours(2),
                                true
                        ))
                ),
                Runnable::run
        );

        AlunoDashboardResponse response = service.execute(ALUNO_ID, List.of("dashboard.view_own", "request.open"));

        assertEquals(2, response.kpis().eventosHoje());
        assertEquals(1, response.proximosEventos().size());
        assertEquals("Oficina Proof of Stay", response.proximosEventos().getFirst().titulo());
        assertEquals("/eventos/" + EVENTO_ID + "/presenca", response.proximosEventos().getFirst().href());
        assertTrue(response.proximosEventos().getFirst().janelaAtiva());
        assertNull(response.kpis().horasFormativas());
        assertNull(response.kpis().certificados());
    }
}
