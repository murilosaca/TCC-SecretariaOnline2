package br.ufpr.sept.so2.modules.iam.application.ports;

public interface PasswordHasher {

    String hash(String raw);

    boolean matches(String raw, String hash);

    /**
     * Equaliza o tempo de resposta quando o identificador não existe (RNF-SEC-09).
     */
    void matchesDummy();
}
