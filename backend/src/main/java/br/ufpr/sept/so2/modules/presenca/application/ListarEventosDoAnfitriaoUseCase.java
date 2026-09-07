package br.ufpr.sept.so2.modules.presenca.application;

import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListarEventosDoAnfitriaoUseCase {

    private final EventoRepository eventoRepository;

    public ListarEventosDoAnfitriaoUseCase(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Evento> execute(UUID anfitriaoId, Pageable pageable) {
        int size = Math.min(Math.max(pageable.getPageSize(), 1), 100);
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "inicioEm");
        return eventoRepository.findByAnfitriao(anfitriaoId, PageRequest.of(pageable.getPageNumber(), size, sort));
    }
}
