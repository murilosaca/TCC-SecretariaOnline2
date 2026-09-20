package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.infrastructure.ComunicacaoProperties
import br.ufpr.sept.so2.modules.iam.application.IamFakes
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class DespacharOutboxUseCaseTest : StringSpec({
    "despacha e marca SENT; segundo ciclo do mesmo id não reenvia" {
        val outbox = IamFakes.Outbox()
        val handler = CapturaHandler()
        val id = UUID.fromString("01800000-0000-7000-8000-0000000000aa")
        outbox.claims += OutboxClaim(id, "PASSWORD_RESET", "{}", 0)
        val useCase = DespacharOutboxUseCase(
            outbox,
            listOf(handler),
            IamFakes.Audit(),
            ComunicacaoProperties(),
        )

        useCase.execute()
        handler.ids.size shouldBe 1
        outbox.sent shouldBe listOf(id)

        useCase.execute()
        handler.ids.size shouldBe 1
    }

    "SMTP falha incrementa tentativas e volta PENDING" {
        val outbox = IamFakes.Outbox()
        val handler = CapturaHandler()
        handler.falhar = true
        val id = UUID.fromString("01800000-0000-7000-8000-0000000000bb")
        outbox.claims += OutboxClaim(id, "PASSWORD_RESET", "{}", 0)
        val useCase = DespacharOutboxUseCase(
            outbox,
            listOf(handler),
            IamFakes.Audit(),
            ComunicacaoProperties(),
        )

        useCase.execute()
        outbox.retried shouldBe listOf(id)
        outbox.sent.isEmpty() shouldBe true
        outbox.failed.isEmpty() shouldBe true
    }

    "tipo sem handler vira SENT no-op" {
        val outbox = IamFakes.Outbox()
        val id = UUID.fromString("01800000-0000-7000-8000-0000000000cc")
        outbox.claims += OutboxClaim(id, "evento.encerrado", "{}", 0)
        val useCase = DespacharOutboxUseCase(
            outbox,
            emptyList(),
            IamFakes.Audit(),
            ComunicacaoProperties(),
        )
        useCase.execute()
        outbox.sent shouldBe listOf(id)
    }
}) {
    private class CapturaHandler : OutboxEventoHandler {
        override val tipos: Set<String> = setOf("PASSWORD_RESET")
        val ids: MutableList<UUID> = ArrayList()
        var falhar: Boolean = false

        override fun handle(evento: OutboxClaim) {
            if (falhar) {
                throw IllegalStateException("smtp")
            }
            ids.add(evento.id)
        }
    }
}
