package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.OffsetDateTime
import java.util.UUID

class ListarInelegiveisPresencaTest : StringSpec({
    val cursoId = UUID.fromString("01800000-0000-7000-8000-0000000000c1")
    val eventoId = UUID.fromString("01800000-0000-7000-8000-0000000000e1")
    val comEntrada = UUID.fromString("01800000-0000-7000-8000-0000000000a1")
    val semEntrada = UUID.fromString("01800000-0000-7000-8000-0000000000a2")
    val agora = OffsetDateTime.parse("2026-09-06T22:00:00Z")

    fun evento(estado: EventoEstado = EventoEstado.EM_ANDAMENTO): Evento = Evento(
        eventoId,
        UUID.fromString("01800000-0000-7000-8000-0000000000aa"),
        "Oficina",
        agora.minusHours(1),
        agora.plusHours(2),
        4,
        AttendanceMode.SECRET_DUAL,
        estado,
        "hash",
        agora.minusMinutes(10),
        agora.plusMinutes(5),
        agora.plusHours(1),
        agora.plusHours(2),
        agora,
        agora,
        cursoId,
    )

    "depois da entrada aberta lista quem não registrou a entrada, pelo nome social" {
        val alunos = mockk<AlunoRepository>()
        val presencas = mockk<PresencaRepository>()
        every { alunos.findByIdCursoIn(listOf(cursoId)) } returns listOf(
            aluno("Bia", null, semEntrada, "GRR20248802", "bia.lista@ufpr.br"),
            aluno("Carlos", "Cacá", comEntrada, "GRR20248803", "carlos.lista@ufpr.br"),
            aluno("Sem conta", null, null, "GRR20248804", "sem.conta@ufpr.br"),
        )
        every { presencas.usuariosComFase(eventoId, FasePresenca.ENTRADA) } returns setOf(comEntrada)

        ListarInelegiveisPresenca(alunos, presencas).nomes(evento()) shouldBe listOf("Bia", "Sem conta")
    }

    "evento ainda agendado não lista inelegíveis" {
        val alunos = mockk<AlunoRepository>()
        val presencas = mockk<PresencaRepository>()

        ListarInelegiveisPresenca(alunos, presencas).nomes(evento(EventoEstado.AGENDADO)) shouldBe emptyList()
    }
})

private fun aluno(nome: String, nomeSocial: String?, usuarioId: UUID?, grr: String, email: String): Aluno {
    val agora = OffsetDateTime.parse("2026-09-06T22:00:00Z")
    return Aluno(
        UUID.randomUUID(),
        nome,
        nomeSocial,
        Grr.of(grr),
        Email.of(email),
        null,
        null,
        UUID.fromString("01800000-0000-7000-8000-0000000000c1"),
        AlunoSituacao.MATRICULADO,
        true,
        agora,
        agora,
        usuarioId,
    )
}
