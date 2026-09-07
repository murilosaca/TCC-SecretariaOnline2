package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence;

import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository;
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca;
import br.ufpr.sept.so2.modules.presenca.domain.Presenca;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PresencaJpaAdapter implements PresencaRepository {

    private final PresencaJpaRepository jpaRepository;

    public PresencaJpaAdapter(PresencaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Presenca save(Presenca presenca) {
        return jpaRepository.save(PresencaJpaEntity.fromDomain(presenca)).toDomain();
    }

    @Override
    public boolean existsByEventoUsuarioFase(UUID eventoId, UUID usuarioId, FasePresenca fase) {
        return jpaRepository.existsByEventoIdAndUsuarioIdAndFase(eventoId, usuarioId, fase.name());
    }

    @Override
    public boolean existsByEventoAndDeviceDeOutroUsuario(UUID eventoId, String deviceUuid, UUID usuarioId) {
        return jpaRepository.existsByEventoIdAndDeviceUuidAndUsuarioIdNot(eventoId, deviceUuid, usuarioId);
    }

    @Override
    public List<Presenca> findByEventoAndUsuario(UUID eventoId, UUID usuarioId) {
        return jpaRepository.findByEventoIdAndUsuarioId(eventoId, usuarioId).stream()
                .map(PresencaJpaEntity::toDomain)
                .toList();
    }

    @Override
    public long countByEvento(UUID eventoId) {
        return jpaRepository.countByEventoId(eventoId);
    }
}
