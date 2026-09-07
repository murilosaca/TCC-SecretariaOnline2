package br.ufpr.sept.so2.modules.presenca.application;

import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository;
import br.ufpr.sept.so2.modules.presenca.application.ports.HostPinPort;
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ObterSessaoHostUseCase {

    private final EventoRepository eventoRepository;
    private final PresencaRepository presencaRepository;
    private final HostPinPort hostPinPort;

    public ObterSessaoHostUseCase(
            EventoRepository eventoRepository,
            PresencaRepository presencaRepository,
            HostPinPort hostPinPort
    ) {
        this.eventoRepository = eventoRepository;
        this.presencaRepository = presencaRepository;
        this.hostPinPort = hostPinPort;
    }

    @Transactional(readOnly = true)
    public AbrirJanelaEntradaUseCase.SessaoHost execute(UUID eventoId, UUID anfitriaoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));
        evento.garantirHospedeiro(anfitriaoId);
        String pin = evento.janelaAtiva(FasePresenca.ENTRADA, OffsetDateTime.now())
                ? hostPinPort.obter(eventoId).orElse(null)
                : null;
        return new AbrirJanelaEntradaUseCase.SessaoHost(evento, pin, presencaRepository.countByEvento(eventoId));
    }
}
