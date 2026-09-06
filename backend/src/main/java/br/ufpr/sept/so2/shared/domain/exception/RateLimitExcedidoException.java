package br.ufpr.sept.so2.shared.domain.exception;

public class RateLimitExcedidoException extends RuntimeException {

    private final int retryAfterSeconds;

    public RateLimitExcedidoException(int retryAfterSeconds) {
        super("Muitas tentativas. Aguarde antes de tentar novamente.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
