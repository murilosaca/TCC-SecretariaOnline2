package br.ufpr.sept.so2.modules.iam.domain;

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import br.ufpr.sept.so2.shared.domain.valueobject.Grr;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    @Test
    void bloqueiaAposDezFalhasELiberaQuandoExpira() {
        Usuario usuario = usuario(false);
        OffsetDateTime agora = OffsetDateTime.parse("2026-03-01T12:00:00Z");
        boolean bloqueou = false;
        for (int i = 0; i < 10; i++) {
            bloqueou = usuario.registrarFalha(agora, 10, 15);
        }
        assertTrue(bloqueou);
        assertTrue(usuario.estaBloqueado(agora.plusMinutes(14)));
        usuario.liberarBloqueioSeExpirado(agora.plusMinutes(15));
        assertFalse(usuario.estaBloqueado(agora.plusMinutes(15)));
        assertEquals(0, usuario.getFalhasConsecutivas());
    }

    @Test
    void primeiroAcessoExigeSenhaNaoAlteradaERegistraLgpd() {
        Usuario usuario = usuario(false);
        OffsetDateTime agora = OffsetDateTime.parse("2026-03-01T12:00:00Z");
        assertTrue(usuario.precisaPrimeiroAcesso());
        usuario.completarPrimeiroAcesso("novo-hash", agora, "127.0.0.1", "Mozilla");
        assertFalse(usuario.precisaPrimeiroAcesso());
        assertTrue(usuario.isSenhaAlterada());
        assertEquals(agora, usuario.getLgpdAceiteEm());
        assertThrows(ConflitoEstadoException.class,
                () -> usuario.completarPrimeiroAcesso("outro", agora, "127.0.0.1", "Mozilla"));
    }

    private static Usuario usuario(boolean senhaAlterada) {
        OffsetDateTime agora = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        return new Usuario(
                UUID.fromString("01800000-0000-7000-8000-0000000000aa"),
                Email.of("aluno.dev@ufpr.br"),
                null,
                Grr.of("GRR20240001"),
                "hash-temp",
                senhaAlterada,
                null,
                null,
                null,
                true,
                0,
                null,
                List.of("dashboard.view_own"),
                agora,
                agora
        );
    }
}
