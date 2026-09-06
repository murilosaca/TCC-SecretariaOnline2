package br.ufpr.sept.so2.modules.iam.infrastructure.security;

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Argon2PasswordHasher implements PasswordHasher {

    private final Argon2PasswordEncoder encoder;
    private final String dummyHash;

    public Argon2PasswordHasher(IamProperties properties) {
        IamProperties.Argon2 argon2 = properties.getArgon2();
        this.encoder = new Argon2PasswordEncoder(
                argon2.getSaltLength(),
                argon2.getHashLength(),
                argon2.getParallelism(),
                argon2.getMemoryKb(),
                argon2.getIterations()
        );
        this.dummyHash = encoder.encode("so2-dummy-timing");
    }

    @Override
    public String hash(String raw) {
        return encoder.encode(raw);
    }

    @Override
    public boolean matches(String raw, String hash) {
        if (raw == null || hash == null) {
            return false;
        }
        return encoder.matches(raw, hash);
    }

    @Override
    public void matchesDummy() {
        encoder.matches("so2-dummy-timing", dummyHash);
    }
}
