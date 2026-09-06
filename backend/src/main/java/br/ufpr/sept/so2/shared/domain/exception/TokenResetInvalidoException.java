package br.ufpr.sept.so2.shared.domain.exception;

public class TokenResetInvalidoException extends RuntimeException {

    public static final String MENSAGEM = "Link inválido ou expirado.";

    public TokenResetInvalidoException() {
        super(MENSAGEM);
    }
}
