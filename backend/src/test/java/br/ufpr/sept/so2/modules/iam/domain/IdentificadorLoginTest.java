package br.ufpr.sept.so2.modules.iam.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentificadorLoginTest {

    @Test
    void normalizaEmailEGrr() {
        IdentificadorLogin email = IdentificadorLogin.tryParse("Aluno.Dev@UFPR.BR").orElseThrow();
        assertEquals(IdentificadorLogin.Tipo.EMAIL, email.getTipo());
        assertEquals("aluno.dev@ufpr.br", email.getValor());

        IdentificadorLogin grr = IdentificadorLogin.tryParse("grr20240001").orElseThrow();
        assertEquals(IdentificadorLogin.Tipo.GRR, grr.getTipo());
        assertEquals("GRR20240001", grr.getValor());

        IdentificadorLogin soDigitos = IdentificadorLogin.tryParse("20240001").orElseThrow();
        assertEquals("GRR20240001", soDigitos.getValor());
    }

    @Test
    void mascaraIdentificadorERejeitaLixo() {
        assertEquals("a***@ufpr.br", IdentificadorLogin.tryParse("aluno.dev@ufpr.br").orElseThrow().mascarado());
        assertEquals("GRR****0001", IdentificadorLogin.tryParse("GRR20240001").orElseThrow().mascarado());
        assertTrue(IdentificadorLogin.tryParse("nao-e-identificador").isEmpty());
        assertTrue(IdentificadorLogin.tryParse("").isEmpty());
    }
}
