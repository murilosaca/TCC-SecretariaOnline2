package br.ufpr.sept.so2.modules.presenca.application;

import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository;
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca;
import br.ufpr.sept.so2.modules.presenca.domain.Presenca;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ObterSessaoPresencaUseCase {

    private final EventoRepository eventoRepository;
    private final PresencaRepository presencaRepository;

    public ObterSessaoPresencaUseCase(EventoRepository eventoRepository, PresencaRepository presencaRepository) {
        this.eventoRepository = eventoRepository;
        this.presencaRepository = presencaRepository;
    }

    @Transactional(readOnly = true)
    public SessaoPresenca execute(UUID eventoId, UUID usuarioId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));
        List<Presenca> registros = presencaRepository.findByEventoAndUsuario(eventoId, usuarioId);
        List<FasePresenca> fases = registros.stream().map(Presenca::getFase).toList();
        return new SessaoPresenca(evento, fases);
    }

    public record SessaoPresenca(Evento evento, List<FasePresenca> fasesConfirmadas) {
    }
}
