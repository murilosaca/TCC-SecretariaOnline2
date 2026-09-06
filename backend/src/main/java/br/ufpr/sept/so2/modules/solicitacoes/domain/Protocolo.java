package br.ufpr.sept.so2.modules.solicitacoes.domain;

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Identificador público estável (RF-F0-006): {@code PROT-YYYY-NNNNN}.
 */
public final class Protocolo {

    private static final Pattern FORMATO = Pattern.compile("PROT-\\d{4}-\\d{5}");

    private final String valor;

    private Protocolo(String valor) {
        this.valor = valor;
    }

    public static Protocolo of(String raw) {
        String normalizado = raw == null ? "" : raw.trim().toUpperCase();
        if (!FORMATO.matcher(normalizado).matches()) {
            throw new DadoInvalidoException("Protocolo inválido. Use o formato PROT-AAAA-NNNNN.");
        }
        return new Protocolo(normalizado);
    }

    public static Protocolo formatar(int ano, int numero) {
        if (ano < 2000 || ano > 2100) {
            throw new DadoInvalidoException("Ano do protocolo inválido.");
        }
        if (numero < 1 || numero > 99999) {
            throw new DadoInvalidoException("Número anual do protocolo inválido.");
        }
        return new Protocolo(String.format("PROT-%d-%05d", ano, numero));
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return valor;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Protocolo protocolo)) {
            return false;
        }
        return valor.equals(protocolo.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}
