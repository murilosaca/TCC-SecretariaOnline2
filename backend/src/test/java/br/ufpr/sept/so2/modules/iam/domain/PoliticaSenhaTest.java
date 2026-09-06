package br.ufpr.sept.so2.modules.iam.domain;

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoliticaSenhaTest {

    @Test
    void aceitaSenhaForte() {
        PoliticaSenha.validar("TroqueEstaSenha1!");
        assertTrue(PoliticaSenha.atende("TroqueEstaSenha1!"));
        assertEquals(4, PoliticaSenha.pontuacao("TroqueEstaSenha1!"));
    }

    @Test
    void rejeitaCurtaESemComplexidade() {
        assertThrows(DadoInvalidoException.class, () -> PoliticaSenha.validar("curta"));
        assertThrows(DadoInvalidoException.class, () -> PoliticaSenha.validar("semmaiuscula1!"));
        assertThrows(DadoInvalidoException.class, () -> PoliticaSenha.validar("SEMMINUSCULA1!"));
        assertThrows(DadoInvalidoException.class, () -> PoliticaSenha.validar("SemNumero!!aa"));
        assertThrows(DadoInvalidoException.class, () -> PoliticaSenha.validar("SemEspecial12aa"));
        assertFalse(PoliticaSenha.atende("fraca"));
        assertEquals(1, PoliticaSenha.pontuacao("abc"));
    }
}
