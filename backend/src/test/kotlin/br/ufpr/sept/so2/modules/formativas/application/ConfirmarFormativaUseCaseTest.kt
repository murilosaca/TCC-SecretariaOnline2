package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort.AlunoRef
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.UUID

class ConfirmarFormativaUseCaseTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-06T22:00:00Z")
    val usuarioId = UUID.fromString("01800000-0000-7000-8000-000000000011")
    val alunoId = UUID.fromString("01800000-0000-7000-8000-0000000000a1")
    val formativaId = UUID.fromString("01800000-0000-7000-8000-0000000000f1")

    fun formativa(): Formativa = Formativa.viaPresenca(
        formativaId,
        alunoId,
        UUID.fromString("01800000-0000-7000-8000-0000000000e1"),
        "Oficina",
        4,
        agora,
    )

    "confirma e publica outbox" {
        val alunoPort = mockk<AlunoPorUsuarioPort>()
        val repo = mockk<FormativaRepository>()
        val outbox = mockk<OutboxPort>()
        every { alunoPort.resolver(usuarioId) } returns AlunoRef(alunoId, false, 120)
        every { repo.findById(formativaId) } returns formativa()
        every { repo.save(any()) } answers { firstArg() }
        every { outbox.enqueue(eq("formativa.confirmada"), any()) } just runs

        val useCase = ConfirmarFormativaUseCase(alunoPort, repo, outbox, ObjectMapper())
        val result = useCase.execute(formativaId, usuarioId)

        result.estado shouldBe FormativaEstado.AGUARDANDO_CAAF
        verify { outbox.enqueue("formativa.confirmada", any()) }
    }

    "egresso e transição ilegal são rejeitados" {
        val alunoPort = mockk<AlunoPorUsuarioPort>()
        val repo = mockk<FormativaRepository>()
        every { alunoPort.resolver(usuarioId) } returns AlunoRef(alunoId, true, 120)
        shouldThrow<AcessoNegadoException> {
            ConfirmarFormativaUseCase(alunoPort, repo, mockk(), ObjectMapper())
                .execute(formativaId, usuarioId)
        }

        every { alunoPort.resolver(usuarioId) } returns AlunoRef(alunoId, false, 120)
        val jaConfirmada = formativa().also { it.confirmar(agora.plusMinutes(1)) }
        every { repo.findById(formativaId) } returns jaConfirmada
        shouldThrow<ConflitoEstadoException> {
            ConfirmarFormativaUseCase(alunoPort, repo, mockk(), ObjectMapper())
                .execute(formativaId, usuarioId)
        }
    }
})
