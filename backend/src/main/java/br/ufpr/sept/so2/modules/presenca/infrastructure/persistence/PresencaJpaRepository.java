package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PresencaJpaRepository extends JpaRepository<PresencaJpaEntity, UUID> {

    boolean existsByEventoIdAndUsuarioIdAndFase(UUID eventoId, UUID usuarioId, String fase);

    boolean existsByEventoIdAndDeviceUuidAndUsuarioIdNot(UUID eventoId, String deviceUuid, UUID usuarioId);

    List<PresencaJpaEntity> findByEventoIdAndUsuarioId(UUID eventoId, UUID usuarioId);

    long countByEventoId(UUID eventoId);
}
