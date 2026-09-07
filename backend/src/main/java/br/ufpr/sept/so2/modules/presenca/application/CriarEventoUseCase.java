package br.ufpr.sept.so2.modules.presenca.application;

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class CriarEventoUseCase {

    private final EventoRepository eventoRepository;
    private final PasswordHasher passwordHasher;

    public CriarEventoUseCase(EventoRepository eventoRepository, PasswordHasher passwordHasher) {
        this.eventoRepository = eventoRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public Evento execute(UUID anfitriaoId, String titulo, OffsetDateTime inicioEm, OffsetDateTime fimEm, int cargaHoraria) {
        return eventoRepository.save(Evento.criar(
                Uuids.v7(),
                anfitriaoId,
                titulo,
                inicioEm,
                fimEm,
                cargaHoraria,
                passwordHasher.hash(PinPresenca.gerar()),
                OffsetDateTime.now()
        ));
    }
}
