package br.ufpr.sept.so2.modules.diplomas.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.UUID

class DiplomaTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-11-15T15:00:00Z")
    val alunoId = UUID.randomUUID()
    val cursoId = UUID.randomUUID()

    fun diploma(): Diploma = Diploma.registrar(
        UUID.randomUUID(),
        alunoId,
        cursoId,
        UUID.randomUUID(),
        "L12-F3-001",
        agora,
        "12",
        "3",
        "2026/2",
        agora,
    )

    "registra como PENDENTE sem PDF" {
        val d = diploma()
        d.situacao shouldBe DiplomaSituacao.PENDENTE
        d.temPdf() shouldBe false
        d.turma shouldBe "2026/2"
    }

    "confirma entrega presencial" {
        val d = diploma()
        d.confirmarEntrega(MetodoEntregaDiploma.PRESENCIAL, agora, agora)
        d.situacao shouldBe DiplomaSituacao.ENTREGUE
        d.metodoEntrega shouldBe MetodoEntregaDiploma.PRESENCIAL
        d.dataEntrega shouldBe agora
    }

    "nao confirma entrega se ja ENTREGUE" {
        val d = diploma()
        d.confirmarEntrega(MetodoEntregaDiploma.CORREIO, agora, agora)
        shouldThrow<ConflitoEstadoException> {
            d.confirmarEntrega(MetodoEntregaDiploma.PRESENCIAL, agora, agora)
        }
    }

    "anexa storage key do MinIO" {
        val d = diploma()
        d.anexarPdf("diplomas/${d.id}.pdf", agora)
        d.temPdf() shouldBe true
        d.storageKey shouldBe "diplomas/${d.id}.pdf"
    }

    "rejeita livro ou folha vazios" {
        shouldThrow<DadoInvalidoException> {
            Diploma.registrar(
                UUID.randomUUID(),
                alunoId,
                cursoId,
                null,
                "X",
                agora,
                " ",
                "1",
                null,
                agora,
            )
        }
    }
})

class AvaliarElegibilidadeColacaoTest : StringSpec({
    val alunoId = UUID.randomUUID()

    "elegivel com TCC e horas" {
        val r = AvaliarElegibilidadeColacao.avaliar(alunoId, "Ana", "GRR20260001", true, 120, 120)
        r.elegivel shouldBe true
        r.bloqueio shouldBe null
    }

    "inelegivel sem TCC" {
        val r = AvaliarElegibilidadeColacao.avaliar(alunoId, "Ana", "GRR20260001", false, 200, 120)
        r.elegivel shouldBe false
        r.bloqueio!!.razao shouldBe "TCC não aprovado"
    }

    "inelegivel por horas" {
        val r = AvaliarElegibilidadeColacao.avaliar(alunoId, "Ana", "GRR20260001", true, 60, 120)
        r.elegivel shouldBe false
        r.bloqueio!!.razao shouldBe "Horas formativas insuficientes: 60/120 h"
    }
})
