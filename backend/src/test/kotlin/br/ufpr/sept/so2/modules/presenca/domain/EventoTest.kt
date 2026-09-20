package br.ufpr.sept.so2.modules.presenca.domain

import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.OffsetDateTime
import java.util.UUID

class EventoTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-06T22:00:00Z")
    val anfitriao = UUID.fromString("01800000-0000-7000-8000-0000000000aa")

    fun evento(
        modo: AttendanceMode,
        inicioJanela: OffsetDateTime? = agora.minusMinutes(5),
        fimJanela: OffsetDateTime? = agora.plusMinutes(25),
        saidaInicio: OffsetDateTime? = null,
        saidaFim: OffsetDateTime? = null,
        estado: EventoEstado = EventoEstado.EM_ANDAMENTO,
    ): Evento = Evento(
        UUID.fromString("01800000-0000-7000-8000-0000000000e1"),
        anfitriao,
        "Oficina Proof of Stay",
        agora.minusHours(1),
        agora.plusHours(2),
        4,
        modo,
        estado,
        "hash-argon2",
        inicioJanela,
        fimJanela,
        saidaInicio,
        saidaFim,
        agora,
        agora,
    )

    "janela ativa somente dentro do intervalo" {
        val secret = evento(AttendanceMode.SECRET_SINGLE)
        secret.janelaAtiva(FasePresenca.ENTRADA, agora) shouldBe true
        secret.janelaAtiva(FasePresenca.ENTRADA, agora.minusMinutes(6)) shouldBe false
        secret.janelaAtiva(FasePresenca.ENTRADA, agora.plusMinutes(26)) shouldBe false
        secret.janelaAtiva(FasePresenca.SAIDA, agora) shouldBe false
    }

    "PIN inválido ou janela fechada produzem a mesma 403" {
        val aberto = evento(AttendanceMode.SECRET_SINGLE, agora.minusMinutes(1), agora.plusMinutes(10))
        val fechado = evento(AttendanceMode.SECRET_SINGLE, agora.minusHours(2), agora.minusHours(1))
        val pin = shouldThrow<AcessoNegadoException> {
            aberto.garantirConfirmacao(FasePresenca.ENTRADA, agora, false)
        }
        val janela = shouldThrow<AcessoNegadoException> {
            fechado.garantirConfirmacao(FasePresenca.ENTRADA, agora, true)
        }
        pin.message shouldBe Evento.CONFIRMACAO_NEGADA
        pin.message shouldBe janela.message
    }

    "SECRET_SINGLE rejeita fase saída com 409" {
        val oficina = evento(AttendanceMode.SECRET_SINGLE, agora.minusMinutes(1), agora.plusMinutes(10))
        shouldThrow<ConflitoEstadoException> {
            oficina.garantirConfirmacao(FasePresenca.SAIDA, agora, true)
        }
    }

    "QR_SINGLE confirma entrada com token válido" {
        val qr = evento(AttendanceMode.QR_SINGLE, agora.minusMinutes(1), agora.plusMinutes(10))
        qr.garantirConfirmacao(FasePresenca.ENTRADA, agora, true)
        qr.pinCompativel(true) shouldBe true
    }

    "SECRET_DUAL confirma saída só com janela de saída ativa" {
        val dual = evento(
            AttendanceMode.SECRET_DUAL,
            agora.minusHours(2),
            agora.minusHours(1),
            agora.minusMinutes(1),
            agora.plusMinutes(10),
        )
        dual.faseDaJanelaAtiva(agora) shouldBe FasePresenca.SAIDA
        dual.garantirConfirmacao(FasePresenca.SAIDA, agora, true)
        shouldThrow<AcessoNegadoException> {
            dual.garantirConfirmacao(FasePresenca.ENTRADA, agora, true)
        }
    }

    "encerrar fecha a janela e impede confirmação" {
        val oficina = evento(AttendanceMode.QR_DUAL, agora.minusMinutes(1), agora.plusMinutes(10))
        oficina.encerrar(agora)
        oficina.estado shouldBe EventoEstado.CONCLUIDO
        oficina.janelaAtiva(FasePresenca.ENTRADA, agora) shouldBe false
        val negada = shouldThrow<AcessoNegadoException> {
            oficina.garantirConfirmacao(FasePresenca.ENTRADA, agora, true)
        }
        negada.message shouldBe Evento.CONFIRMACAO_NEGADA
    }

    "abrir janela de entrada vale para QR e SECRET" {
        val qr = Evento.criar(
            UUID.fromString("01800000-0000-7000-8000-0000000000e2"),
            anfitriao,
            "QR",
            agora.minusHours(1),
            agora.plusHours(1),
            4,
            AttendanceMode.QR_SINGLE,
            agora,
        )
        qr.abrirJanela(FasePresenca.ENTRADA, "hash-novo", agora, 15)
        qr.estado shouldBe EventoEstado.EM_ANDAMENTO
        qr.janelaAtiva(FasePresenca.ENTRADA, agora) shouldBe true
        qr.pinHash shouldBe "hash-novo"
    }

    "abrir janela de saída em SINGLE gera 409" {
        val single = evento(AttendanceMode.SECRET_SINGLE, null, null, estado = EventoEstado.AGENDADO)
        single.abrirJanela(FasePresenca.ENTRADA, "hash-e", agora, 15)
        shouldThrow<ConflitoEstadoException> {
            single.abrirJanela(FasePresenca.SAIDA, "hash-s", agora.plusMinutes(20), 15)
        }
    }

    "abrir saída fecha a entrada e gera PIN novo" {
        val dual = Evento.criar(
            UUID.fromString("01800000-0000-7000-8000-0000000000e3"),
            anfitriao,
            "Dual",
            agora.minusHours(1),
            agora.plusHours(3),
            4,
            AttendanceMode.SECRET_DUAL,
            agora,
        )
        dual.abrirJanela(FasePresenca.ENTRADA, "hash-e", agora, 15)
        dual.abrirJanela(FasePresenca.SAIDA, "hash-s", agora.plusMinutes(20), 15)
        dual.janelaAtiva(FasePresenca.ENTRADA, agora.plusMinutes(20)) shouldBe false
        dual.janelaAtiva(FasePresenca.SAIDA, agora.plusMinutes(20)) shouldBe true
        dual.pinHash shouldBe "hash-s"
        dual.faseDaJanelaAtiva(agora.plusMinutes(20)) shouldBe FasePresenca.SAIDA
    }

    "renovar QR exige janela ativa e modo QR" {
        val secret = evento(AttendanceMode.SECRET_SINGLE)
        shouldThrow<ConflitoEstadoException> {
            secret.renovarSegredo("outro", agora)
        }
        val qr = evento(AttendanceMode.QR_SINGLE, agora.minusMinutes(1), agora.plusMinutes(10))
        qr.renovarSegredo("hash-rotado", agora)
        qr.pinHash shouldBe "hash-rotado"
        qr.janelaEntradaFim shouldNotBe null
    }

    "criar persiste o modo informado sem hash em claro" {
        val criado = Evento.criar(
            UUID.fromString("01800000-0000-7000-8000-0000000000e4"),
            anfitriao,
            "Oficina QR dual",
            agora,
            agora.plusHours(2),
            2,
            AttendanceMode.QR_DUAL,
            agora,
        )
        criado.attendanceMode shouldBe AttendanceMode.QR_DUAL
        criado.estado shouldBe EventoEstado.AGENDADO
        criado.pinHash shouldBe null
    }
})
