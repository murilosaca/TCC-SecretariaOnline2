package br.ufpr.sept.so2.shared.domain.valueobject;

import java.util.Objects;

public final class Cpf {

    private final String value;

    private Cpf(String value) {
        this.value = value;
    }

    public static Cpf of(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.length() != 11 || !isValid(digits)) {
            throw new IllegalArgumentException("CPF inválido: " + raw);
        }
        return new Cpf(digits);
    }

    public String masked() {
        return "***." + value.substring(3, 6) + "." + value.substring(6, 9) + "-**";
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
        if (!(other instanceof Cpf cpf)) {
            return false;
        }
        return value.equals(cpf.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    private static boolean isValid(String digits) {
        if (digits.chars().distinct().count() == 1) {
            return false;
        }
        int d1 = checksum(digits, 10);
        int d2 = checksum(digits, 11);
        return d1 == (digits.charAt(9) - '0') && d2 == (digits.charAt(10) - '0');
    }

    private static int checksum(String digits, int weightStart) {
        int sum = 0;
        for (int i = 0; i < weightStart - 1; i++) {
            sum += (digits.charAt(i) - '0') * (weightStart - i);
        }
        int result = 11 - (sum % 11);
        return result >= 10 ? 0 : result;
    }
}
