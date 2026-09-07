package br.ufpr.sept.so2.modules.presenca.application;

import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ListarEventosDoAlunoUseCase {

    private final EventoRepository eventoRepository;

    public ListarEventosDoAlunoUseCase(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Evento> execute(String audience, Pageable pageable) {
        if (!"me".equalsIgnoreCase(audience)) {
            throw new AcessoNegadoException("Neste momento só é possível listar os eventos disponíveis para você.");
        }
        int size = Math.min(Math.max(pageable.getPageSize(), 1), 100);
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "inicioEm");
        return eventoRepository.findAbertosParaAluno(
                OffsetDateTime.now(),
                PageRequest.of(pageable.getPageNumber(), size, sort)
        );
    }
}
