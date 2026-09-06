package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence;

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository;
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao;
import br.ufpr.sept.so2.modules.solicitacoes.domain.SolicitacaoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SolicitacaoJpaAdapter implements SolicitacaoRepository {

    private final SolicitacaoJpaRepository jpaRepository;
    private final SolicitacaoEventoJpaRepository eventoRepository;

    public SolicitacaoJpaAdapter(
            SolicitacaoJpaRepository jpaRepository,
            SolicitacaoEventoJpaRepository eventoRepository
    ) {
        this.jpaRepository = jpaRepository;
        this.eventoRepository = eventoRepository;
    }

    @Override
    public Solicitacao save(Solicitacao solicitacao) {
        SolicitacaoJpaEntity entity = jpaRepository.findById(solicitacao.getId())
                .orElseGet(() -> SolicitacaoJpaEntity.fromDomain(solicitacao));
        entity.merge(solicitacao);
        jpaRepository.save(entity);
        for (SolicitacaoEvento evento : solicitacao.getEventos()) {
            if (!eventoRepository.existsById(evento.id())) {
                eventoRepository.save(SolicitacaoEventoJpaEntity.fromDomain(solicitacao.getId(), evento));
            }
        }
        return carregar(solicitacao.getId()).orElseThrow();
    }

    @Override
    public Optional<Solicitacao> findById(UUID id) {
        return carregar(id);
    }

    @Override
    public Optional<Solicitacao> findByProtocolo(String protocolo) {
        return jpaRepository.findByProtocolo(protocolo)
                .map(entity -> entity.toDomain(eventosDe(entity.getId())));
    }

    @Override
    public Page<Solicitacao> findMinhas(
            UUID solicitanteId,
            String estado,
            String tipoCodigo,
            Integer ano,
            Pageable pageable
    ) {
        String anoPrefixo = ano == null ? null : "PROT-" + ano + "-%";
        return jpaRepository.findMinhas(solicitanteId, estado, tipoCodigo, anoPrefixo, pageable)
                .map(SolicitacaoJpaEntity::toDomainSemEventos);
    }

    private Optional<Solicitacao> carregar(UUID id) {
        return jpaRepository.findById(id).map(entity -> entity.toDomain(eventosDe(id)));
    }

    private List<SolicitacaoEvento> eventosDe(UUID solicitacaoId) {
        return eventoRepository.findBySolicitacaoIdOrderByCreatedAtDesc(solicitacaoId).stream()
                .map(SolicitacaoEventoJpaEntity::toDomain)
                .toList();
    }
}
