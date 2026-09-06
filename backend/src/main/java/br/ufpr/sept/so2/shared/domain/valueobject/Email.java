package br.ufpr.sept.so2.shared.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String raw) {
        String normalized = raw == null ? "" : raw.trim().toLowerCase();
        if (!EMAIL_REGEX.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Formato de email inválido: " + raw);
        }
        return new Email(normalized);
    }

    public boolean isInstitutional() {
        return value.endsWith("@ufpr.br");
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
        if (!(other instanceof Email email)) {
            return false;
        }
        return value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
