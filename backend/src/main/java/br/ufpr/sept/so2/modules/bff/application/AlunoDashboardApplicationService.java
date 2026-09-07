package br.ufpr.sept.so2.modules.bff.application;

import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse;
import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse.KpisResponse;
import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse.PendenciaResponse;
import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse.PeriodoVigenteResponse;
import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse.ProximoEventoResponse;
import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse.SaudacaoResponse;
import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse.UltimaSolicitacaoResponse;
import br.ufpr.sept.so2.modules.bff.application.ports.AlunoIdentidadeQueryPort;
import br.ufpr.sept.so2.modules.bff.application.ports.AlunoIdentidadeQueryPort.AlunoIdentidade;
import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort;
import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort.EventoResumo;
import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort.EventosDashboard;
import br.ufpr.sept.so2.modules.bff.application.ports.PeriodoVigenteQueryPort;
import br.ufpr.sept.so2.modules.bff.application.ports.PeriodoVigenteQueryPort.PeriodoVigenteResumo;
import br.ufpr.sept.so2.modules.bff.application.ports.SolicitacoesDashboardQueryPort;
import br.ufpr.sept.so2.modules.bff.application.ports.SolicitacoesDashboardQueryPort.SolicitacoesDashboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Service
public class AlunoDashboardApplicationService {

    private static final Logger LOG = LoggerFactory.getLogger(AlunoDashboardApplicationService.class);
    public static final String AUTHORITY_DASHBOARD = "dashboard.view_own";
    public static final String AUTHORITY_REQUEST_OPEN = "request.open";

    private final AlunoIdentidadeQueryPort identidadeQueryPort;
    private final PeriodoVigenteQueryPort periodoVigenteQueryPort;
    private final SolicitacoesDashboardQueryPort solicitacoesDashboardQueryPort;
    private final EventosDashboardQueryPort eventosDashboardQueryPort;
    private final Executor executor;

    @Autowired
    public AlunoDashboardApplicationService(
            AlunoIdentidadeQueryPort identidadeQueryPort,
            PeriodoVigenteQueryPort periodoVigenteQueryPort,
            SolicitacoesDashboardQueryPort solicitacoesDashboardQueryPort,
            EventosDashboardQueryPort eventosDashboardQueryPort
    ) {
        this(
                identidadeQueryPort,
                periodoVigenteQueryPort,
                solicitacoesDashboardQueryPort,
                eventosDashboardQueryPort,
                java.util.concurrent.ForkJoinPool.commonPool()
        );
    }

    AlunoDashboardApplicationService(
            AlunoIdentidadeQueryPort identidadeQueryPort,
            PeriodoVigenteQueryPort periodoVigenteQueryPort,
            SolicitacoesDashboardQueryPort solicitacoesDashboardQueryPort,
            EventosDashboardQueryPort eventosDashboardQueryPort,
            Executor executor
    ) {
        this.identidadeQueryPort = identidadeQueryPort;
        this.periodoVigenteQueryPort = periodoVigenteQueryPort;
        this.solicitacoesDashboardQueryPort = solicitacoesDashboardQueryPort;
        this.eventosDashboardQueryPort = eventosDashboardQueryPort;
        this.executor = executor;
    }

    public AlunoDashboardResponse execute(UUID usuarioId, List<String> authorities) {
        CompletableFuture<AlunoIdentidade> identidadeF = isoladoAsync(
                "identidade",
                () -> identidadeQueryPort.consultar(usuarioId)
        );
        CompletableFuture<PeriodoBloco> periodoF = isoladoAsync(
                "periodo",
                this::carregarPeriodo
        );
        CompletableFuture<SolicitacoesDashboard> solicitacoesF = isoladoAsync(
                "solicitacoes",
                () -> solicitacoesDashboardQueryPort.consultar(usuarioId)
        );
        CompletableFuture<EventosDashboard> eventosF = isoladoAsync(
                "eventos",
                () -> eventosDashboardQueryPort.consultar(OffsetDateTime.now())
        );
        AlunoIdentidade identidade = identidadeF.join();
        PeriodoBloco periodo = periodoF.join();
        SolicitacoesDashboard solicitacoes = solicitacoesF.join();
        EventosDashboard eventos = eventosF.join();

        SaudacaoResponse saudacao = identidade == null
                ? new SaudacaoResponse("Aluno", null)
                : new SaudacaoResponse(identidade.nome(), identidade.curso());
        PeriodoVigenteResponse periodoVigente = periodo == null ? null : periodo.vigente();
        Boolean alertaPeriodo = periodo == null ? null : periodo.alertaAusente();
        KpisResponse kpis = new KpisResponse(
                null,
                solicitacoes == null ? null : solicitacoes.abertas(),
                eventos == null ? null : eventos.hoje(),
                null
        );
        List<PendenciaResponse> pendencias = solicitacoes == null
                ? null
                : solicitacoes.pendencias().stream()
                .map(item -> new PendenciaResponse(item.id(), item.titulo(), item.estado(), item.href()))
                .toList();
        List<UltimaSolicitacaoResponse> ultimas = solicitacoes == null
                ? null
                : solicitacoes.ultimas().stream()
                .map(item -> new UltimaSolicitacaoResponse(
                        item.id(),
                        item.protocolo(),
                        item.tipoNome(),
                        item.estado(),
                        item.prazoEm(),
                        item.slaVencido()
                ))
                .toList();
        List<ProximoEventoResponse> proximos = eventos == null
                ? null
                : eventos.proximos().stream().map(AlunoDashboardApplicationService::toEvento).toList();
        return new AlunoDashboardResponse(
                saudacao,
                periodoVigente,
                alertaPeriodo,
                kpis,
                pendencias,
                ultimas,
                proximos,
                links(authorities)
        );
    }

    private PeriodoBloco carregarPeriodo() {
        return periodoVigenteQueryPort.consultar(LocalDate.now())
                .map(resumo -> new PeriodoBloco(toResponse(resumo), false))
                .orElseGet(() -> new PeriodoBloco(null, true));
    }

    private static PeriodoVigenteResponse toResponse(PeriodoVigenteResumo resumo) {
        return new PeriodoVigenteResponse(
                resumo.id(),
                resumo.ano(),
                resumo.semestre(),
                resumo.rotulo(),
                resumo.inicio(),
                resumo.fim()
        );
    }

    private static ProximoEventoResponse toEvento(EventoResumo item) {
        return new ProximoEventoResponse(
                item.id(),
                item.titulo(),
                item.inicioEm(),
                item.fimEm(),
                item.janelaAtiva(),
                "/eventos/" + item.id() + "/presenca"
        );
    }

    private static Map<String, String> links(List<String> authorities) {
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", "/bff/dashboard/aluno");
        if (authorities != null && authorities.contains(AUTHORITY_REQUEST_OPEN)) {
            links.put("novaSolicitacao", "/solicitacoes/nova");
        }
        return links;
    }

    private <T> CompletableFuture<T> isoladoAsync(String bloco, Supplier<T> consulta) {
        return CompletableFuture.supplyAsync(() -> isolado(bloco, consulta), executor);
    }

    private <T> T isolado(String bloco, Supplier<T> consulta) {
        try {
            return consulta.get();
        } catch (CompletionException ex) {
            LOG.warn("BFF dashboard: bloco {} indisponível", bloco);
            return null;
        } catch (RuntimeException ex) {
            LOG.warn("BFF dashboard: bloco {} indisponível", bloco);
            return null;
        }
    }

    private record PeriodoBloco(PeriodoVigenteResponse vigente, boolean alertaAusente) {
    }
}
