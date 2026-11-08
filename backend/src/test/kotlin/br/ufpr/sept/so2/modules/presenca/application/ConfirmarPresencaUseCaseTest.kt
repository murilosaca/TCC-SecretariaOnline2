package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaPorPresencaPort
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.modules.presenca.domain.Presenca
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
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
import java.util.Optional
import java.util.UUID

class ConfirmarPresencaUseCaseTest : StringSpec({
    val eventoId = UUID.fromString("01800000-0000-7000-8000-0000000000e1")
    val usuarioId = UUID.fromString("01800000-0000-7000-8000-0000000000a1")
    val objectMapper = ObjectMapper()

    fun oficina(modo: AttendanceMode, saida: Boolean = false): Evento {
        val agora = OffsetDateTime.now()
        val inicio = agora.minusMinutes(5)
        val fim = agora.plusMinutes(20)
        return Evento(
            eventoId,
            UUID.fromString("01800000-0000-7000-8000-0000000000aa"),
            "Oficina",
            agora.minusHours(1),
            agora.plusHours(2),
            4,
            modo,
            EventoEstado.EM_ANDAMENTO,
            "hash",
            if (saida) agora.minusHours(1) else inicio,
            if (saida) agora.minusMinutes(30) else fim,
            if (saida) inicio else null,
            if (saida) fim else null,
            agora,
            agora,
            null,
        )
    }

    fun useCase(
        eventos: EventoRepository,
        presencas: PresencaRepository,
        hasher: PasswordHasher = mockk {
            every { matches(any(), any()) } returns true
        },
        formativa: FormativaPorPresencaPort = mockk(relaxed = true),
        outbox: OutboxPort = mockk(relaxed = true),
        audit: AuditLogPort = mockk(relaxed = true),
        publico: PublicoAlvoEvento = mockk(relaxed = true),
    ) = ConfirmarPresencaUseCase(eventos, presencas, hasher, outbox, audit, objectMapper, formativa, publico)

    "DUAL na entrada não dispara formativa; saída dispara" {
        val dual = oficina(AttendanceMode.SECRET_DUAL)
        val eventos = mockk<EventoRepository>()
        val presencas = mockk<PresencaRepository>()
        val formativa = mockk<FormativaPorPresencaPort>(relaxed = true)
        every { eventos.findById(eventoId) } returns Optional.of(dual)
        every { presencas.existsByEventoUsuarioFase(eventoId, usuarioId, FasePresenca.ENTRADA) } returns false
        every { presencas.vincularDispositivo(eventoId, "device-uuid-1", usuarioId) } returns true
        every { presencas.save(any()) } answers { firstArg() }
        every { presencas.findByEventoAndUsuario(eventoId, usuarioId) } returns emptyList()

        useCase(eventos, presencas, formativa = formativa).execute(
            eventoId,
            usuarioId,
            "123456",
            null,
            "device-uuid-1",
            "ENTRADA",
            "127.0.0.1",
        )
        verify(exactly = 0) { formativa.criarPendenteSeAusente(any(), any(), any(), any()) }

        val saida = oficina(AttendanceMode.QR_DUAL, saida = true)
        every { eventos.findById(eventoId) } returns Optional.of(saida)
        every { presencas.existsByEventoUsuarioFase(eventoId, usuarioId, FasePresenca.SAIDA) } returns false
        every { presencas.existsByEventoUsuarioFase(eventoId, usuarioId, FasePresenca.ENTRADA) } returns true
        every { presencas.vincularDispositivo(eventoId, "device-uuid-1", usuarioId) } returns true
        every { presencas.save(any()) } answers { firstArg() }
        every { presencas.findByEventoAndUsuario(eventoId, usuarioId) } returns listOf(
            Presenca.registrar(UUID.randomUUID(), eventoId, usuarioId, FasePresenca.ENTRADA, "device-uuid-1", OffsetDateTime.now()),
        )

        useCase(eventos, presencas, formativa = formativa).execute(
            eventoId,
            usuarioId,
            null,
            "token-qr",
            "device-uuid-1",
            "SAIDA",
            "127.0.0.1",
        )
        verify(exactly = 1) {
            formativa.criarPendenteSeAusente(eventoId, usuarioId, "Oficina", 4)
        }
    }

    "saída sem entrada é conflito" {
        val dual = oficina(AttendanceMode.SECRET_DUAL, saida = true)
        val eventos = mockk<EventoRepository>()
        val presencas = mockk<PresencaRepository>()
        every { eventos.findById(eventoId) } returns Optional.of(dual)
        every { presencas.existsByEventoUsuarioFase(eventoId, usuarioId, FasePresenca.SAIDA) } returns false
        every { presencas.vincularDispositivo(any(), any(), any()) } returns true
        every { presencas.existsByEventoUsuarioFase(eventoId, usuarioId, FasePresenca.ENTRADA) } returns false

        shouldThrow<ConflitoEstadoException> {
            useCase(eventos, presencas).execute(
                eventoId,
                usuarioId,
                "123456",
                null,
                "device-uuid-1",
                "SAIDA",
                null,
            )
        }
        verify(exactly = 0) { presencas.save(any()) }
    }

    "evento de outro curso responde como não encontrado" {
        val cursoEvento = UUID.fromString("01800000-0000-7000-8000-0000000000c1")
        val outroCurso = UUID.fromString("01800000-0000-7000-8000-0000000000c2")
        val evento = oficina(AttendanceMode.SECRET_SINGLE)
        evento.idCurso = cursoEvento
        val eventos = mockk<EventoRepository>()
        val presencas = mockk<PresencaRepository>()
        val alunos = mockk<AlunoRepository>()
        every { eventos.findById(eventoId) } returns Optional.of(evento)
        every { alunos.findByIdUsuario(usuarioId) } returns Optional.of(alunoDe(outroCurso))

        shouldThrow<RecursoNaoEncontradoException> {
            useCase(eventos, presencas, publico = PublicoAlvoEvento(alunos)).execute(
                eventoId,
                usuarioId,
                "123456",
                null,
                "device-uuid-1",
                "ENTRADA",
                null,
            )
        }
        verify(exactly = 0) { presencas.save(any()) }
    }

    "dispositivo de outro aluno neste evento é conflito" {
        val evento = oficina(AttendanceMode.SECRET_SINGLE)
        val eventos = mockk<EventoRepository>()
        val presencas = mockk<PresencaRepository>()
        every { eventos.findById(eventoId) } returns Optional.of(evento)
        every { presencas.existsByEventoUsuarioFase(eventoId, usuarioId, FasePresenca.ENTRADA) } returns false
        every { presencas.vincularDispositivo(eventoId, "device-uuid-1", usuarioId) } returns false

        shouldThrow<ConflitoEstadoException> {
            useCase(eventos, presencas).execute(
                eventoId,
                usuarioId,
                "123456",
                null,
                "device-uuid-1",
                "ENTRADA",
                null,
            )
        }.message shouldBe "Este dispositivo já foi usado neste evento."
        verify(exactly = 0) { presencas.save(any()) }
    }
})

private fun alunoDe(cursoId: UUID): Aluno {
    val agora = OffsetDateTime.now()
    return Aluno(
        UUID.fromString("01800000-0000-7000-8000-0000000000b1"),
        "Ana",
        null,
        Grr.of("GRR20248801"),
        Email.of("ana.curso@ufpr.br"),
        null,
        null,
        cursoId,
        AlunoSituacao.MATRICULADO,
        true,
        agora,
        agora,
        UUID.fromString("01800000-0000-7000-8000-0000000000a1"),
    )
}
