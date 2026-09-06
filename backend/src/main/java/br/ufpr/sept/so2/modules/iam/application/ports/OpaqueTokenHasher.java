package br.ufpr.sept.so2.modules.iam.application.ports;

public interface OpaqueTokenHasher {

    String hash(String rawToken);
}
