package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence;

import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EventoJpaAdapter implements EventoRepository {

    private final EventoJpaRepository jpaRepository;

    public EventoJpaAdapter(EventoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Evento save(Evento evento) {
        EventoJpaEntity entity = jpaRepository.findById(evento.getId())
                .orElseGet(() -> EventoJpaEntity.fromDomain(evento));
        entity.merge(evento);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Evento> findById(UUID id) {
        return jpaRepository.findById(id).map(EventoJpaEntity::toDomain);
    }

    @Override
    public Page<Evento> findAbertosParaAluno(OffsetDateTime agora, Pageable pageable) {
        return jpaRepository.findAbertosParaAluno(agora, pageable).map(EventoJpaEntity::toDomain);
    }

    @Override
    public List<Evento> findEmAndamentoComJanelaNoDia(OffsetDateTime inicioDia, OffsetDateTime fimDia) {
        return jpaRepository.findEmAndamentoComJanelaNoDia(inicioDia, fimDia).stream()
                .map(EventoJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Page<Evento> findByAnfitriao(UUID anfitriaoId, Pageable pageable) {
        return jpaRepository.findByIdAnfitriao(anfitriaoId, pageable).map(EventoJpaEntity::toDomain);
    }

    @Override
    public boolean existsByTitulo(String titulo) {
        return jpaRepository.existsByTituloIgnoreCase(titulo);
    }

    @Override
    public Optional<Evento> findByTitulo(String titulo) {
        return jpaRepository.findByTituloIgnoreCase(titulo).map(EventoJpaEntity::toDomain);
    }
}
