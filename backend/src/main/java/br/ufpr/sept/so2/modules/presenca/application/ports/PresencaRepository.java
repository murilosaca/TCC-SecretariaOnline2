package br.ufpr.sept.so2.modules.presenca.application.ports;

import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca;
import br.ufpr.sept.so2.modules.presenca.domain.Presenca;

import java.util.List;
import java.util.UUID;

public interface PresencaRepository {

    Presenca save(Presenca presenca);

    boolean existsByEventoUsuarioFase(UUID eventoId, UUID usuarioId, FasePresenca fase);

    boolean existsByEventoAndDeviceDeOutroUsuario(UUID eventoId, String deviceUuid, UUID usuarioId);

    List<Presenca> findByEventoAndUsuario(UUID eventoId, UUID usuarioId);

    long countByEvento(UUID eventoId);
}
