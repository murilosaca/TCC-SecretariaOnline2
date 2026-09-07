package br.ufpr.sept.so2.modules.presenca.domain;

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;

public enum FasePresenca {
    ENTRADA,
    SAIDA;

    public static FasePresenca from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DadoInvalidoException("Fase de presença obrigatória.");
        }
        try {
            return FasePresenca.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DadoInvalidoException("Fase de presença inválida.");
        }
    }
}
