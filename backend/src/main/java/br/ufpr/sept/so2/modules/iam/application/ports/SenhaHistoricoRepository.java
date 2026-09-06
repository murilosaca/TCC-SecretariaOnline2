package br.ufpr.sept.so2.modules.iam.application.ports;

import java.util.List;
import java.util.UUID;

public interface SenhaHistoricoRepository {

    void append(UUID usuarioId, String senhaHash);

    List<String> findLastHashes(UUID usuarioId, int limite);
}
