package br.ufpr.sept.so2.modules.presenca.domain;

import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException;
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventoTest {

    private static final OffsetDateTime AGORA = OffsetDateTime.parse("2026-09-06T22:00:00Z");
    private static final UUID ANFITRIAO = UUID.fromString("01800000-0000-7000-8000-0000000000aa");

    @Test
    void janelaAtivaSomenteDentroDoIntervalo() {
        Evento evento = secretSingle(AGORA.minusMinutes(5), AGORA.plusMinutes(25));
        assertTrue(evento.janelaAtiva(FasePresenca.ENTRADA, AGORA));
        assertFalse(evento.janelaAtiva(FasePresenca.ENTRADA, AGORA.minusMinutes(6)));
        assertFalse(evento.janelaAtiva(FasePresenca.ENTRADA, AGORA.plusMinutes(26)));
        assertFalse(evento.janelaAtiva(FasePresenca.SAIDA, AGORA));
    }

    @Test
    void pinInvalidoOuJanelaFechadaProduzemMesma403() {
        Evento aberto = secretSingle(AGORA.minusMinutes(1), AGORA.plusMinutes(10));
        Evento fechado = secretSingle(AGORA.minusHours(2), AGORA.minusHours(1));
        AcessoNegadoException pin = assertThrows(
                AcessoNegadoException.class,
                () -> aberto.garantirConfirmacao(AttendanceMode.SECRET_SINGLE, FasePresenca.ENTRADA, AGORA, false)
        );
        AcessoNegadoException janela = assertThrows(
                AcessoNegadoException.class,
                () -> fechado.garantirConfirmacao(AttendanceMode.SECRET_SINGLE, FasePresenca.ENTRADA, AGORA, true)
        );
        assertEquals(Evento.CONFIRMACAO_NEGADA, pin.getMessage());
        assertEquals(pin.getMessage(), janela.getMessage());
    }

    @Test
    void outroModoOuFaseSaidaGeram409() {
        Evento evento = secretSingle(AGORA.minusMinutes(1), AGORA.plusMinutes(10));
        assertThrows(
                ConflitoEstadoException.class,
                () -> evento.garantirConfirmacao(AttendanceMode.QR_SINGLE, FasePresenca.ENTRADA, AGORA, true)
        );
        assertThrows(
                ConflitoEstadoException.class,
                () -> evento.garantirConfirmacao(AttendanceMode.SECRET_SINGLE, FasePresenca.SAIDA, AGORA, true)
        );
    }

    @Test
    void pinValidoNaJanelaPermiteSecretSingle() {
        Evento evento = secretSingle(AGORA.minusMinutes(1), AGORA.plusMinutes(10));
        evento.garantirConfirmacao(AttendanceMode.SECRET_SINGLE, FasePresenca.ENTRADA, AGORA, true);
        assertTrue(evento.pinCompativel(true));
        assertFalse(evento.pinCompativel(false));
    }

    @Test
    void encerrarFechaJanelaEImpedeConfirmacao() {
        Evento evento = secretSingle(AGORA.minusMinutes(1), AGORA.plusMinutes(10));
        evento.encerrar(AGORA);
        assertEquals(EventoEstado.CONCLUIDO, evento.getEstado());
        assertFalse(evento.janelaAtiva(FasePresenca.ENTRADA, AGORA));
        AcessoNegadoException negada = assertThrows(
                AcessoNegadoException.class,
                () -> evento.garantirConfirmacao(AttendanceMode.SECRET_SINGLE, FasePresenca.ENTRADA, AGORA, true)
        );
        assertEquals(Evento.CONFIRMACAO_NEGADA, negada.getMessage());
    }

    @Test
    void abrirJanelaDeOutroModoGera409() {
        Evento qr = new Evento(
                UUID.fromString("01800000-0000-7000-8000-0000000000e2"),
                ANFITRIAO,
                "QR",
                AGORA.minusHours(1),
                AGORA.plusHours(1),
                4,
                AttendanceMode.QR_SINGLE,
                EventoEstado.AGENDADO,
                "hash-argon2",
                null,
                null,
                null,
                null,
                AGORA,
                AGORA
        );
        assertThrows(
                ConflitoEstadoException.class,
                () -> qr.abrirJanelaEntrada("hash-novo", AGORA, 15)
        );
    }

    private static Evento secretSingle(OffsetDateTime inicioJanela, OffsetDateTime fimJanela) {
        return new Evento(
                UUID.fromString("01800000-0000-7000-8000-0000000000e1"),
                ANFITRIAO,
                "Oficina Proof of Stay",
                inicioJanela.minusHours(1),
                fimJanela.plusHours(1),
                4,
                AttendanceMode.SECRET_SINGLE,
                EventoEstado.EM_ANDAMENTO,
                "hash-argon2",
                inicioJanela,
                fimJanela,
                null,
                null,
                AGORA,
                AGORA
        );
    }
}
