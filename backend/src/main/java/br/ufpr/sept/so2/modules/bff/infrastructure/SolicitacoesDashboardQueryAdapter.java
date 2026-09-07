package br.ufpr.sept.so2.modules.bff.infrastructure;

import br.ufpr.sept.so2.modules.bff.application.ports.SolicitacoesDashboardQueryPort;
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository;
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class SolicitacoesDashboardQueryAdapter implements SolicitacoesDashboardQueryPort {

    private static final Set<String> ESTADOS_ABERTOS = Set.of("EM_ANALISE", "EM_AJUSTE");
    private static final String ESTADO_PENDENCIA = "EM_AJUSTE";

    private final SolicitacaoRepository solicitacaoRepository;

    public SolicitacoesDashboardQueryAdapter(SolicitacaoRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitacoesDashboard consultar(UUID solicitanteId) {
        Sort recente = Sort.by(Sort.Direction.DESC, "createdAt");
        OffsetDateTime agora = OffsetDateTime.now();
        List<SolicitacaoResumo> ultimas = solicitacaoRepository
                .findMinhas(solicitanteId, null, null, null, PageRequest.of(0, 5, recente))
                .getContent()
                .stream()
                .map(item -> toResumo(item, agora))
                .toList();
        List<PendenciaResumo> pendencias = solicitacaoRepository
                .findMinhas(solicitanteId, ESTADO_PENDENCIA, null, null, PageRequest.of(0, 3, recente))
                .getContent()
                .stream()
                .map(SolicitacoesDashboardQueryAdapter::toPendencia)
                .toList();
        return new SolicitacoesDashboard(contarAbertas(solicitanteId), pendencias, ultimas);
    }

    private int contarAbertas(UUID solicitanteId) {
        int total = 0;
        for (String estado : ESTADOS_ABERTOS) {
            total += (int) solicitacaoRepository
                    .findMinhas(solicitanteId, estado, null, null, PageRequest.of(0, 1))
                    .getTotalElements();
        }
        return total;
    }

    private static SolicitacaoResumo toResumo(Solicitacao solicitacao, OffsetDateTime agora) {
        return new SolicitacaoResumo(
                solicitacao.getId(),
                solicitacao.getProtocolo().getValor(),
                solicitacao.getTipoNome(),
                solicitacao.getEstado(),
                solicitacao.getPrazoEm(),
                solicitacao.prazoVencido(agora)
        );
    }

    private static PendenciaResumo toPendencia(Solicitacao solicitacao) {
        return new PendenciaResumo(
                solicitacao.getId(),
                solicitacao.getTipoNome(),
                solicitacao.getEstado(),
                "/solicitacoes/" + solicitacao.getId()
        );
    }
}
