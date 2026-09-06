package br.ufpr.sept.so2.shared.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Grr {

    private static final Pattern GRR_REGEX = Pattern.compile("^GRR\\d{8}$");

    private final String value;

    private Grr(String value) {
        this.value = value;
    }

    public static Grr of(String raw) {
        String normalized = raw == null ? "" : raw.trim().toUpperCase();
        if (!GRR_REGEX.matcher(normalized).matches()) {
            throw new IllegalArgumentException("GRR inválido: " + raw + " (esperado GRR + 8 dígitos)");
        }
        return new Grr(normalized);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Grr grr)) {
            return false;
        }
        return value.equals(grr.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
