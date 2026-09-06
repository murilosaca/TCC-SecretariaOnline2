package br.ufpr.sept.so2.modules.iam.application.ports;

import java.util.UUID;

public interface AuditLogPort {

    void append(String tipo, UUID atorId, String payload, String ip);
}
