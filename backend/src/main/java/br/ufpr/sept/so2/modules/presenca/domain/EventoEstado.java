package br.ufpr.sept.so2.modules.presenca.domain;

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;

public enum EventoEstado {
    AGENDADO,
    EM_ANDAMENTO,
    CONCLUIDO;

    public static EventoEstado from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DadoInvalidoException("Estado do evento obrigatório.");
        }
        try {
            return EventoEstado.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DadoInvalidoException("Estado do evento inválido.");
        }
    }
}
