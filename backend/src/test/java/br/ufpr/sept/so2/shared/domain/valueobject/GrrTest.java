package br.ufpr.sept.so2.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GrrTest {

    @Test
    void aceitaGrrValido() {
        Grr grr = Grr.of("grr20241234");
        assertEquals("GRR20241234", grr.getValue());
    }

    @Test
    void rejeitaFormatoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> Grr.of("20241234"));
    }
}
