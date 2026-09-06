package br.ufpr.sept.so2.modules.iam.domain;

import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import br.ufpr.sept.so2.shared.domain.valueobject.Grr;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Identificador aceito no login: e-mail institucional/pessoal ou GRR (RF-F0-001-a).
 */
public final class IdentificadorLogin {

    public enum Tipo {
        EMAIL,
        GRR
    }

    private static final Pattern SO_DIGITOS = Pattern.compile("^\\d{8}$");

    private final Tipo tipo;
    private final String valor;

    private IdentificadorLogin(Tipo tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    public static Optional<IdentificadorLogin> tryParse(String raw) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        try {
            if (trimmed.contains("@")) {
                return Optional.of(new IdentificadorLogin(Tipo.EMAIL, Email.of(trimmed).getValue()));
            }
            String candidato = SO_DIGITOS.matcher(trimmed).matches()
                    ? "GRR" + trimmed
                    : trimmed;
            return Optional.of(new IdentificadorLogin(Tipo.GRR, Grr.of(candidato).getValue()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public String mascarado() {
        if (tipo == Tipo.GRR) {
            if (valor.length() < 7) {
                return "GRR****";
            }
            return "GRR****" + valor.substring(valor.length() - 4);
        }
        int at = valor.indexOf('@');
        if (at <= 1) {
            return "***" + valor.substring(Math.max(at, 0));
        }
        return valor.charAt(0) + "***" + valor.substring(at);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IdentificadorLogin that)) {
            return false;
        }
        return tipo == that.tipo && valor.equals(that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, valor);
    }
}
