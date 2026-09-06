package br.ufpr.sept.so2.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTest {

    @Test
    void identificaEmailInstitucional() {
        assertTrue(Email.of("aluno@ufpr.br").isInstitutional());
        assertFalse(Email.of("aluno@gmail.com").isInstitutional());
    }
}
