package br.ufpr.sept.so2.modules.presenca.application.ports;

import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoRepository {

    Evento save(Evento evento);

    Optional<Evento> findById(UUID id);

    Page<Evento> findAbertosParaAluno(OffsetDateTime agora, Pageable pageable);

    List<Evento> findEmAndamentoComJanelaNoDia(OffsetDateTime inicioDia, OffsetDateTime fimDia);

    Page<Evento> findByAnfitriao(UUID anfitriaoId, Pageable pageable);

    boolean existsByTitulo(String titulo);

    Optional<Evento> findByTitulo(String titulo);
}
