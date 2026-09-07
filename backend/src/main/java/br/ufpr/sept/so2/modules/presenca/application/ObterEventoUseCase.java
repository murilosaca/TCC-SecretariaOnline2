package br.ufpr.sept.so2.modules.presenca.application;

import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ObterEventoUseCase {

    private final EventoRepository eventoRepository;

    public ObterEventoUseCase(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Transactional(readOnly = true)
    public Evento execute(UUID id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));
    }
}
