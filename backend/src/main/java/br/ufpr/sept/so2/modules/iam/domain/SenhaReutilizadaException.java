package br.ufpr.sept.so2.modules.iam.domain;

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;

public class SenhaReutilizadaException extends DadoInvalidoException {

    public SenhaReutilizadaException(String message) {
        super(message);
    }

    public static SenhaReutilizadaException historico() {
        return new SenhaReutilizadaException(
                "Esta senha já foi utilizada recentemente. Escolha uma senha diferente.");
    }

    public static SenhaReutilizadaException temporaria() {
        return new SenhaReutilizadaException(
                "A nova senha não pode ser igual à senha temporária gerada pelo sistema.");
    }
}
