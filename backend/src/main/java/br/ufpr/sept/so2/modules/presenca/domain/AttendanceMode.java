package br.ufpr.sept.so2.modules.presenca.domain;

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;

public enum AttendanceMode {
    QR_SINGLE,
    QR_DUAL,
    SECRET_SINGLE,
    SECRET_DUAL;

    public boolean isDual() {
        return this == QR_DUAL || this == SECRET_DUAL;
    }

    public boolean isSecret() {
        return this == SECRET_SINGLE || this == SECRET_DUAL;
    }

    public boolean exercitadoNesteSprint() {
        return this == SECRET_SINGLE;
    }

    public static AttendanceMode from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DadoInvalidoException("Modo de presença obrigatório.");
        }
        try {
            return AttendanceMode.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DadoInvalidoException("Modo de presença inválido.");
        }
    }
}
