package br.ufpr.sept.so2.modules.academico.domain;

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodoLetivoTest {

    @Test
    void rejeitaSemestreInvalido() {
        assertThrows(DadoInvalidoException.class, () -> periodo(2026, 3, "2026-02-01", "2026-07-15"));
    }

    @Test
    void rejeitaIntervaloInvertido() {
        assertThrows(DadoInvalidoException.class, () -> periodo(2026, 1, "2026-07-15", "2026-02-01"));
    }

    @Test
    void detectaSobreposicaoEPeriodoVigente() {
        PeriodoLetivo primeiro = periodo(2026, 1, "2026-02-01", "2026-07-15");
        PeriodoLetivo segundo = periodo(2026, 2, "2026-07-01", "2026-12-20");
        PeriodoLetivo adjacente = periodo(2026, 2, "2026-07-15", "2026-12-20");

        assertTrue(primeiro.sobrepoe(segundo));
        assertFalse(primeiro.sobrepoe(adjacente));
        assertTrue(primeiro.vigenteEm(LocalDate.parse("2026-03-10")));
        assertFalse(primeiro.vigenteEm(LocalDate.parse("2026-08-01")));
    }

    private static PeriodoLetivo periodo(int ano, int semestre, String inicio, String fim) {
        OffsetDateTime agora = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        return new PeriodoLetivo(
                UUID.fromString("01800000-0000-7000-8000-000000000001"),
                ano,
                semestre,
                LocalDate.parse(inicio),
                LocalDate.parse(fim),
                true,
                agora,
                agora
        );
    }
}
