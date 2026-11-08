package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

class DefinirAtivoCursoUseCaseTest : StringSpec({
    "desativar preserva o curso e não apaga o histórico" {
        val id = UUID.fromString("01800000-0000-7000-8000-0000000000c1")
        val usuario = UUID.fromString("01800000-0000-7000-8000-0000000000a1")
        val agora = OffsetDateTime.parse("2026-11-08T12:00:00Z")
        val curso = Curso(id, "Análise e Desenvolvimento de Sistemas", "TADS", "TADS-SEPT", null, 80, true, agora, agora)
        val cursos = mockk<CursoRepository>()
        val escopo = mockk<CursoEscopoPort>()
        val audit = mockk<AuditLogPort>(relaxed = true)
        every { escopo.cursoIdsDoUsuario(usuario) } returns setOf(id)
        every { cursos.findById(id) } returns Optional.of(curso)
        every { cursos.save(curso) } returns curso
        val useCase = DefinirAtivoCursoUseCase(cursos, escopo, audit, ObjectMapper())

        val salvo = useCase.execute(id, usuario, false, "127.0.0.1")

        salvo.ativo shouldBe false
        salvo.id shouldBe id
        verify(exactly = 0) { cursos.deleteById(any()) }
        verify { audit.appendDiff("curso.atualizado", usuario, any(), match { it.contains("\"entidade\":\"curso\"") }, "127.0.0.1") }
    }

    "curso fora do escopo responde como não encontrado" {
        val id = UUID.fromString("01800000-0000-7000-8000-0000000000c2")
        val usuario = UUID.fromString("01800000-0000-7000-8000-0000000000a1")
        val cursos = mockk<CursoRepository>()
        val escopo = mockk<CursoEscopoPort>()
        every { escopo.cursoIdsDoUsuario(usuario) } returns emptySet()
        val useCase = DefinirAtivoCursoUseCase(cursos, escopo, mockk(relaxed = true), ObjectMapper())

        shouldThrow<RecursoNaoEncontradoException> {
            useCase.execute(id, usuario, false, null)
        }
        verify(exactly = 0) { cursos.deleteById(any()) }
    }
})
