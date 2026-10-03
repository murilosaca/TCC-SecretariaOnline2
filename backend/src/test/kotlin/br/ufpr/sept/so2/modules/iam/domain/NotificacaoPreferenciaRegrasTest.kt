package br.ufpr.sept.so2.modules.iam.domain

import br.ufpr.sept.so2.modules.iam.application.CampoPatch
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID

class NotificacaoPreferenciaRegrasTest : StringSpec({
    "CRITICAL desligado é 422 e o restante pode mudar" {
        val agora = OffsetDateTime.parse("2026-10-01T12:00:00Z")
        val pref = NotificacaoPreferenciaRegras.padrao(
            UUID.fromString("01800000-0000-7000-8000-0000000000aa"),
            agora,
        )
        shouldThrow<DadoInvalidoException> {
            NotificacaoPreferenciaRegras.aplicar(
                pref,
                mapOf(PrioridadeNotificacao.CRITICAL to mapOf(CanalNotificacao.EMAIL to false)),
                false,
                null,
                false,
                null,
                null,
                agora,
            )
        }
        NotificacaoPreferenciaRegras.aplicar(
            pref,
            mapOf(PrioridadeNotificacao.MEDIUM to mapOf(CanalNotificacao.EMAIL to false)),
            true,
            LocalTime.of(22, 0),
            true,
            LocalTime.of(7, 0),
            ModoDigest.RESUMO,
            agora,
        )
        pref.canais[PrioridadeNotificacao.CRITICAL]?.get(CanalNotificacao.EMAIL) shouldBe true
        pref.canais[PrioridadeNotificacao.MEDIUM]?.get(CanalNotificacao.EMAIL) shouldBe false
        pref.canais[PrioridadeNotificacao.MEDIUM]?.get(CanalNotificacao.IN_APP) shouldBe true
        pref.digest shouldBe ModoDigest.RESUMO
        pref.dndInicio shouldBe LocalTime.of(22, 0)
    }

    "DND exige os dois horários" {
        val agora = OffsetDateTime.parse("2026-10-01T12:00:00Z")
        val pref = NotificacaoPreferenciaRegras.padrao(
            UUID.fromString("01800000-0000-7000-8000-0000000000aa"),
            agora,
        )
        shouldThrow<DadoInvalidoException> {
            NotificacaoPreferenciaRegras.aplicar(
                pref,
                null,
                true,
                LocalTime.of(22, 0),
                true,
                null,
                ModoDigest.IMEDIATO,
                agora,
            )
        }
        CampoPatch.ausente<LocalTime?>().presente shouldBe false
    }
})
