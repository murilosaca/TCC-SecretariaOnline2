package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort
import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort.Cadastro
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.DocumentoEstagio
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.EstagioSituacao
import br.ufpr.sept.so2.modules.estagio.domain.TipoDocumentoEstagio
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class RegistrarEstagioUseCaseTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-26T12:00:00Z")
    val cursoId = UUID.fromString("01800000-0000-7000-8000-00000000e101")
    val outroCurso = UUID.fromString("01800000-0000-7000-8000-00000000e102")
    val atorId = UUID.fromString("01800000-0000-7000-8000-00000000e103")
    val alunoId = UUID.fromString("01800000-0000-7000-8000-00000000e104")
    val orientadorId = UUID.fromString("01800000-0000-7000-8000-00000000e105")
    val inicio = LocalDate.parse("2026-04-01")
    val fim = LocalDate.parse("2026-10-30")

    "registro nasce ativo, sem orientador, com TCE e relatório final" {
        val repo = MemoriaEstagios()
        val outbox = MemoriaOutbox()
        val audit = MemoriaAudit()
        val salvo = registrar(repo, outbox, audit, setOf(cursoId), mapOf(alunoId to cursoId))
            .execute(atorId, alunoId, "  Empresa Nova  ", " Ana ", inicio, fim, "127.0.0.1")
        salvo.situacao shouldBe EstagioSituacao.ATIVO
        salvo.idOrientador.shouldBeNull()
        salvo.idCurso shouldBe cursoId
        salvo.empresa shouldBe "Empresa Nova"
        salvo.supervisor shouldBe "Ana"
        salvo.documentos.map { it.tipo } shouldContainExactly listOf(
            TipoDocumentoEstagio.TCE,
            TipoDocumentoEstagio.RELATORIO_FINAL,
        )
        salvo.documentos.all { it.estado.name == "PENDENTE" && it.obrigatorio } shouldBe true
        outbox.tipos shouldBe listOf("estagio.registrado")
        audit.tipos shouldBe listOf("estagio.registrado")
    }

    "aluno inexistente e aluno fora do escopo devolvem a mesma ausência" {
        val alunos = mapOf(alunoId to outroCurso)
        val fora = shouldThrow<RecursoNaoEncontradoException> {
            registrar(MemoriaEstagios(), MemoriaOutbox(), MemoriaAudit(), setOf(cursoId), alunos)
                .execute(atorId, alunoId, "Empresa", "Ana", inicio, fim, null)
        }
        val ausente = shouldThrow<RecursoNaoEncontradoException> {
            registrar(MemoriaEstagios(), MemoriaOutbox(), MemoriaAudit(), setOf(cursoId), alunos)
                .execute(atorId, UUID.fromString("01800000-0000-7000-8000-00000000e199"), "Empresa", "Ana", inicio, fim, null)
        }
        fora.message shouldBe ausente.message
    }

    "edição de ativo preserva o orientador e o concluído não muda" {
        val aberto = Estagio.abrir(
            UUID.fromString("01800000-0000-7000-8000-00000000e106"),
            alunoId,
            cursoId,
            orientadorId,
            "Empresa",
            "Ana",
            inicio,
            fim,
            agora,
            listOf(
                DocumentoEstagio.pendente(UUID.fromString("01800000-0000-7000-8000-00000000e107"), TipoDocumentoEstagio.TCE),
                DocumentoEstagio.pendente(UUID.fromString("01800000-0000-7000-8000-00000000e108"), TipoDocumentoEstagio.RELATORIO_FINAL),
            ),
        )
        val repo = MemoriaEstagios(mutableListOf(aberto))
        val audit = MemoriaAudit()
        val atualizado = atualizar(repo, MemoriaOutbox(), audit, setOf(cursoId))
            .execute(atorId, aberto.id, alunoId, "Empresa Nova", "Paulo", inicio.plusDays(1), fim, "10.0.0.1")
        atualizado.empresa shouldBe "Empresa Nova"
        atualizado.idOrientador shouldBe orientadorId
        atualizado.situacao shouldBe EstagioSituacao.ATIVO
        audit.tipos shouldBe listOf("estagio.atualizado")

        val concluido = Estagio(
            UUID.fromString("01800000-0000-7000-8000-00000000e109"),
            alunoId,
            cursoId,
            null,
            "Empresa",
            "Ana",
            inicio,
            fim,
            EstagioSituacao.CONCLUIDO,
            agora,
            agora,
            listOf(DocumentoEstagio.pendente(UUID.fromString("01800000-0000-7000-8000-00000000e10a"), TipoDocumentoEstagio.TCE)),
        )
        val repoConcluido = MemoriaEstagios(mutableListOf(concluido))
        shouldThrow<ConflitoEstadoException> {
            atualizar(repoConcluido, MemoriaOutbox(), MemoriaAudit(), setOf(cursoId))
                .execute(atorId, concluido.id, alunoId, "Outra", "Paulo", inicio, fim, null)
        }
        concluido.empresa shouldBe "Empresa"
    }

    "trocar o aluno ou editar estágio de outro curso não segue" {
        val aberto = Estagio.abrir(
            UUID.fromString("01800000-0000-7000-8000-00000000e10b"),
            alunoId,
            cursoId,
            null,
            "Empresa",
            "Ana",
            inicio,
            fim,
            agora,
            listOf(DocumentoEstagio.pendente(UUID.fromString("01800000-0000-7000-8000-00000000e10c"), TipoDocumentoEstagio.TCE)),
        )
        shouldThrow<DadoInvalidoException> {
            atualizar(MemoriaEstagios(mutableListOf(aberto)), MemoriaOutbox(), MemoriaAudit(), setOf(cursoId))
                .execute(atorId, aberto.id, UUID.fromString("01800000-0000-7000-8000-00000000e10d"), "Empresa", "Ana", inicio, fim, null)
        }
        val deOutroCurso = Estagio.abrir(
            UUID.fromString("01800000-0000-7000-8000-00000000e10e"),
            alunoId,
            outroCurso,
            null,
            "Fora",
            "Ana",
            inicio,
            fim,
            agora,
            listOf(DocumentoEstagio.pendente(UUID.fromString("01800000-0000-7000-8000-00000000e10f"), TipoDocumentoEstagio.TCE)),
        )
        val inexistente = UUID.fromString("01800000-0000-7000-8000-00000000e110")
        val fora = shouldThrow<RecursoNaoEncontradoException> {
            atualizar(MemoriaEstagios(mutableListOf(deOutroCurso)), MemoriaOutbox(), MemoriaAudit(), setOf(cursoId))
                .execute(atorId, deOutroCurso.id, alunoId, "Fora", "Ana", inicio, fim, null)
        }
        val ausente = shouldThrow<RecursoNaoEncontradoException> {
            atualizar(MemoriaEstagios(), MemoriaOutbox(), MemoriaAudit(), setOf(cursoId))
                .execute(atorId, inexistente, alunoId, "Fora", "Ana", inicio, fim, null)
        }
        fora.message shouldBe ausente.message
    }
})

private fun registrar(
    repo: EstagioRepository,
    outbox: OutboxPort,
    audit: AuditLogPort,
    cursos: Set<UUID>,
    alunos: Map<UUID, UUID>,
) = RegistrarEstagioUseCase(repo, AlunosMemoria(alunos), CursosMemoria(cursos), outbox, audit, ObjectMapper())

private fun atualizar(
    repo: EstagioRepository,
    outbox: OutboxPort,
    audit: AuditLogPort,
    cursos: Set<UUID>,
) = AtualizarEstagioUseCase(repo, CursosMemoria(cursos), outbox, audit, ObjectMapper())

private class CursosMemoria(private val ids: Set<UUID>) : CursoEscopoPort {
    override fun cursoIdsDoUsuario(usuarioId: UUID): Set<UUID> = ids

    override fun cursoIdDoAlunoPorGrrOuEmail(grr: String?, emailInstitucional: String?): UUID? = null
}

private class AlunosMemoria(private val cursos: Map<UUID, UUID>) : AlunoEstagioPort {
    override fun resolver(usuarioId: UUID): AlunoEstagioPort.AlunoRef? = null

    override fun nomeDe(alunoId: UUID): String? = null

    override fun cadastro(alunoId: UUID): Cadastro? = cursos[alunoId]?.let { Cadastro(alunoId, it) }
}

private class MemoriaEstagios(
    private val itens: MutableList<Estagio> = mutableListOf(),
) : EstagioRepository {
    override fun save(estagio: Estagio): Estagio {
        itens.removeIf { it.id == estagio.id }
        itens += estagio
        return estagio
    }

    override fun findById(id: UUID): Estagio? = itens.find { it.id == id }

    override fun findByAluno(alunoId: UUID, situacao: EstagioSituacao?, pageable: Pageable): Page<Estagio> = Page.empty()

    override fun findParaRevisao(orientadorId: UUID, situacao: EstagioSituacao?, pageable: Pageable): Page<Estagio> =
        Page.empty()

    override fun findByCursos(cursoIds: Collection<UUID>, situacao: EstagioSituacao?, pageable: Pageable): Page<Estagio> =
        Page.empty()

    override fun findPoolCoe(cursoIds: Collection<UUID>, usuarioId: UUID): List<Estagio> = emptyList()

    override fun countSemOrientador(cursoIds: Collection<UUID>): Long = 0

    override fun countAtribuidosAtivos(cursoIds: Collection<UUID>, orientadorId: UUID): Long = 0

    override fun countConcluidosDesde(cursoIds: Collection<UUID>, inicio: OffsetDateTime): Long = 0

    override fun countCargaAtiva(cursoIds: Collection<UUID>, orientadorIds: Collection<UUID>): Map<UUID, Long> =
        emptyMap()

    override fun existsByAluno(alunoId: UUID): Boolean = false

    override fun existsSemOrientador(cursoId: UUID): Boolean = false
}

private class MemoriaOutbox : OutboxPort {
    val tipos = mutableListOf<String>()

    override fun enqueue(tipo: String, payload: String) {
        tipos += tipo
    }

    override fun claimPending(limit: Int, staleBefore: OffsetDateTime) = emptyList<br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim>()

    override fun markSent(id: UUID) = Unit

    override fun markFailed(id: UUID, tentativas: Int, lastError: String?) = Unit

    override fun markPendingRetry(id: UUID, tentativas: Int, lastError: String?) = Unit
}

private class MemoriaAudit : AuditLogPort {
    val tipos = mutableListOf<String>()

    override fun append(tipo: String, atorId: UUID?, payload: String?, ip: String?) {
        tipos += tipo
    }
}
