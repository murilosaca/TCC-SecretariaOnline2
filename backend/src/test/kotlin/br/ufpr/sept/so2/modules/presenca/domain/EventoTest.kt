package br.ufpr.sept.so2.modules.presenca.domain

import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.UUID

class EventoTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-06T22:00:00Z")
    val anfitriao = UUID.fromString("01800000-0000-7000-8000-0000000000aa")

    fun secretSingle(inicioJanela: OffsetDateTime, fimJanela: OffsetDateTime): Evento = Evento(
        UUID.fromString("01800000-0000-7000-8000-0000000000e1"),
        anfitriao,
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
        agora,
        agora,
    )

    "janela ativa somente dentro do intervalo" {
        val evento = secretSingle(agora.minusMinutes(5), agora.plusMinutes(25))
        evento.janelaAtiva(FasePresenca.ENTRADA, agora) shouldBe true
        evento.janelaAtiva(FasePresenca.ENTRADA, agora.minusMinutes(6)) shouldBe false
        evento.janelaAtiva(FasePresenca.ENTRADA, agora.plusMinutes(26)) shouldBe false
        evento.janelaAtiva(FasePresenca.SAIDA, agora) shouldBe false
    }

    "PIN inválido ou janela fechada produzem a mesma 403" {
        val aberto = secretSingle(agora.minusMinutes(1), agora.plusMinutes(10))
        val fechado = secretSingle(agora.minusHours(2), agora.minusHours(1))
        val pin = shouldThrow<AcessoNegadoException> {
            aberto.garantirConfirmacao(AttendanceMode.SECRET_SINGLE, FasePresenca.ENTRADA, agora, false)
        }
        val janela = shouldThrow<AcessoNegadoException> {
            fechado.garantirConfirmacao(AttendanceMode.SECRET_SINGLE, FasePresenca.ENTRADA, agora, true)
        }
        pin.message shouldBe Evento.CONFIRMACAO_NEGADA
        pin.message shouldBe janela.message
    }

    "outro modo ou fase saída geram 409" {
        val evento = secretSingle(agora.minusMinutes(1), agora.plusMinutes(10))
        shouldThrow<ConflitoEstadoException> {
            evento.garantirConfirmacao(AttendanceMode.QR_SINGLE, FasePresenca.ENTRADA, agora, true)
        }
        shouldThrow<ConflitoEstadoException> {
            evento.garantirConfirmacao(AttendanceMode.SECRET_SINGLE, FasePresenca.SAIDA, agora, true)
        }
    }

    "PIN válido na janela permite SECRET_SINGLE" {
        val evento = secretSingle(agora.minusMinutes(1), agora.plusMinutes(10))
        evento.garantirConfirmacao(AttendanceMode.SECRET_SINGLE, FasePresenca.ENTRADA, agora, true)
        evento.pinCompativel(true) shouldBe true
        evento.pinCompativel(false) shouldBe false
    }

    "encerrar fecha a janela e impede confirmação" {
        val evento = secretSingle(agora.minusMinutes(1), agora.plusMinutes(10))
        evento.encerrar(agora)
        evento.estado shouldBe EventoEstado.CONCLUIDO
        evento.janelaAtiva(FasePresenca.ENTRADA, agora) shouldBe false
        val negada = shouldThrow<AcessoNegadoException> {
            evento.garantirConfirmacao(AttendanceMode.SECRET_SINGLE, FasePresenca.ENTRADA, agora, true)
        }
        negada.message shouldBe Evento.CONFIRMACAO_NEGADA
    }

    "abrir janela de outro modo gera 409" {
        val qr = Evento(
            UUID.fromString("01800000-0000-7000-8000-0000000000e2"),
            anfitriao,
            "QR",
            agora.minusHours(1),
            agora.plusHours(1),
            4,
            AttendanceMode.QR_SINGLE,
            EventoEstado.AGENDADO,
            "hash-argon2",
            null,
            null,
            null,
            null,
            agora,
            agora,
        )
        shouldThrow<ConflitoEstadoException> {
            qr.abrirJanelaEntrada("hash-novo", agora, 15)
        }
    }
})
