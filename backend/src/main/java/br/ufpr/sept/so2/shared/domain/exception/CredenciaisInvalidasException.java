package br.ufpr.sept.so2.shared.domain.exception;

public class CredenciaisInvalidasException extends RuntimeException {

    public static final String MENSAGEM =
            "Credenciais inválidas. Verifique seus dados e tente novamente.";

    public CredenciaisInvalidasException() {
        super(MENSAGEM);
    }
}
