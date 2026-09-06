package br.ufpr.sept.so2.modules.iam.application.ports;

import java.time.OffsetDateTime;

public interface JtiBlacklistRepository {

    void add(String jti, OffsetDateTime expiresAt);

    boolean contains(String jti);
}
