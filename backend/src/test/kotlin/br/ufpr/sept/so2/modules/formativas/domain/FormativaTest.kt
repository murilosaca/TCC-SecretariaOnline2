package br.ufpr.sept.so2.modules.formativas.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.UUID

class FormativaTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-06T22:00:00Z")
    val alunoId = UUID.fromString("01800000-0000-7000-8000-0000000000a1")
    val eventoId = UUID.fromString("01800000-0000-7000-8000-0000000000e1")

    fun pendente(): Formativa = Formativa.viaPresenca(
        UUID.fromString("01800000-0000-7000-8000-0000000000f1"),
        alunoId,
        eventoId,
        "Oficina Proof of Stay",
        4,
        agora,
    )

    "criar via presença fica PENDENTE_CONFIRMACAO" {
        val formativa = pendente()
        formativa.origem shouldBe FormativaOrigem.PRESENCA_VALIDADA
        formativa.estado shouldBe FormativaEstado.PENDENTE_CONFIRMACAO
        formativa.idEvento shouldBe eventoId
        formativa.cargaHoraria shouldBe 4
        formativa.chaveUnica() shouldBe (eventoId to alunoId)
    }

    "UNIQUE evento+aluno é idempotente e rejeita chave distinta" {
        val primeira = pendente()
        val mesma = Formativa.viaPresencaOuExistente(
            primeira,
            UUID.fromString("01800000-0000-7000-8000-0000000000f2"),
            alunoId,
            eventoId,
            "Oficina Proof of Stay",
            4,
            agora,
        )
        mesma.id shouldBe primeira.id

        shouldThrow<ConflitoEstadoException> {
            Formativa.viaPresencaOuExistente(
                primeira,
                UUID.fromString("01800000-0000-7000-8000-0000000000f3"),
                UUID.fromString("01800000-0000-7000-8000-0000000000a2"),
                eventoId,
                "Outra",
                2,
                agora,
            )
        }
    }

    "confirmar move para AGUARDANDO_CAAF" {
        val formativa = pendente()
        formativa.confirmar(agora.plusMinutes(1))
        formativa.estado shouldBe FormativaEstado.AGUARDANDO_CAAF
    }

    "cancelar move para CANCELADA e não volta sozinha" {
        val formativa = pendente()
        formativa.cancelar(agora.plusMinutes(1))
        formativa.estado shouldBe FormativaEstado.CANCELADA
        shouldThrow<ConflitoEstadoException> { formativa.confirmar(agora.plusMinutes(2)) }
        formativa.estado shouldBe FormativaEstado.CANCELADA
    }

    "transição ilegal após confirmar é 409" {
        val formativa = pendente()
        formativa.confirmar(agora.plusMinutes(1))
        shouldThrow<ConflitoEstadoException> { formativa.confirmar(agora.plusMinutes(2)) }
        shouldThrow<ConflitoEstadoException> { formativa.cancelar(agora.plusMinutes(2)) }
        formativa.estado shouldBe FormativaEstado.AGUARDANDO_CAAF
    }

    "origem comprovante não é aceita" {
        shouldThrow<ConflitoEstadoException> { Formativa.viaComprovante() }
    }

    "aprovar a partir de AGUARDANDO_CAAF guarda parecer e não muda cargaHoraria" {
        val formativa = pendente()
        formativa.confirmar(agora.plusMinutes(1))
        val revisor = UUID.fromString("01800000-0000-7000-8000-0000000000c1")
        formativa.aprovar("Horas confirmadas pela presença validada.", revisor, agora.plusMinutes(2))
        formativa.estado shouldBe FormativaEstado.APROVADA
        formativa.cargaHoraria shouldBe 4
        formativa.parecer shouldBe "Horas confirmadas pela presença validada."
        formativa.idRevisor shouldBe revisor
        shouldThrow<ConflitoEstadoException> {
            formativa.indeferir("Parecer suficiente para tentar de novo.", revisor, agora.plusMinutes(3))
        }
        formativa.estado shouldBe FormativaEstado.APROVADA
    }

    "indeferir exige parecer de 20 caracteres e rejeita transição ilegal" {
        val formativa = pendente()
        val revisor = UUID.fromString("01800000-0000-7000-8000-0000000000c1")
        shouldThrow<ConflitoEstadoException> {
            formativa.aprovar("Ainda pendente de confirmação do aluno.", revisor, agora)
        }
        formativa.confirmar(agora.plusMinutes(1))
        shouldThrow<DadoInvalidoException> {
            formativa.indeferir("curto", revisor, agora.plusMinutes(2))
        }
        formativa.estado shouldBe FormativaEstado.AGUARDANDO_CAAF
        formativa.indeferir("Atividade não corresponde ao evento validado.", revisor, agora.plusMinutes(2))
        formativa.estado shouldBe FormativaEstado.INDEFERIDA
    }
})
