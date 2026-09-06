package br.ufpr.sept.so2.modules.iam.domain;

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;

public final class PoliticaSenha {

    public static final int TAMANHO_MINIMO = 12;

    private PoliticaSenha() {
    }

    public static void validar(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO) {
            throw new DadoInvalidoException("A senha deve ter no mínimo 12 caracteres.");
        }
        if (!temMaiuscula(senha)) {
            throw new DadoInvalidoException("A senha deve conter ao menos uma letra maiúscula.");
        }
        if (!temMinuscula(senha)) {
            throw new DadoInvalidoException("A senha deve conter ao menos uma letra minúscula.");
        }
        if (!temDigito(senha)) {
            throw new DadoInvalidoException("A senha deve conter ao menos um número.");
        }
        if (!temEspecial(senha)) {
            throw new DadoInvalidoException("A senha deve conter ao menos um caractere especial.");
        }
    }

    public static boolean atende(String senha) {
        return senha != null
                && senha.length() >= TAMANHO_MINIMO
                && temMaiuscula(senha)
                && temMinuscula(senha)
                && temDigito(senha)
                && temEspecial(senha);
    }

    /**
     * Medidor de 4 níveis (F0.3 / F1.2): 0 vazia, 1 fraca, 2 parcial, 3 maioria, 4 forte.
     */
    public static int pontuacao(String senha) {
        if (senha == null || senha.isEmpty()) {
            return 0;
        }
        int criterios = 0;
        if (senha.length() >= TAMANHO_MINIMO) {
            criterios++;
        }
        if (temMaiuscula(senha)) {
            criterios++;
        }
        if (temMinuscula(senha)) {
            criterios++;
        }
        if (temDigito(senha)) {
            criterios++;
        }
        if (temEspecial(senha)) {
            criterios++;
        }
        if (criterios <= 1 || senha.length() < 8) {
            return 1;
        }
        if (criterios <= 3) {
            return 2;
        }
        if (criterios == 4) {
            return 3;
        }
        return 4;
    }

    public static boolean temMaiuscula(String senha) {
        return senha.chars().anyMatch(Character::isUpperCase);
    }

    public static boolean temMinuscula(String senha) {
        return senha.chars().anyMatch(Character::isLowerCase);
    }

    public static boolean temDigito(String senha) {
        return senha.chars().anyMatch(Character::isDigit);
    }

    public static boolean temEspecial(String senha) {
        return senha.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
    }
}
