package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.UsuarioExistenciaPort
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.tcc.application.ports.AlunoTccPort
import br.ufpr.sept.so2.modules.tcc.application.ports.AlunoTccPort.Cadastro
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.MembroBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.PapelBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.modules.tcc.domain.TccEstado
import br.ufpr.sept.so2.modules.tcc.domain.TccSituacao
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class RegistrarTccUseCaseTest : StringSpec({
    val cursoId = UUID.fromString("01800000-0000-7000-8000-00000000c101")
    val outroCurso = UUID.fromString("01800000-0000-7000-8000-00000000c102")
    val atorId = UUID.fromString("01800000-0000-7000-8000-00000000c103")
    val alunoId = UUID.fromString("01800000-0000-7000-8000-00000000c104")
    val orientadorId = UUID.fromString("01800000-0000-7000-8000-00000000c105")
    val bancaId = UUID.fromString("01800000-0000-7000-8000-00000000c106")
    val defesa = LocalDate.parse("2026-11-12")
    val entrega = LocalDate.parse("2026-10-03")

    "registro nasce ativo em elaboração com banca e trilha" {
        val repo = MemoriaTccs()
        val outbox = MemoriaOutbox()
        val audit = MemoriaAudit()
        val salvo = registrar(repo, outbox, audit, setOf(cursoId), mapOf(alunoId to cursoId), setOf(orientadorId, bancaId))
            .execute(
                atorId,
                alunoId,
                "  Plataforma SO2  ",
                defesa,
                entrega,
                listOf(orientadorId to "ORIENTADOR", bancaId to "BANCA"),
                "127.0.0.1",
            )
        salvo.situacao shouldBe TccSituacao.ATIVO
        salvo.estado shouldBe TccEstado.EM_ELABORACAO
        salvo.idCurso shouldBe cursoId
        salvo.titulo shouldBe "Plataforma SO2"
        salvo.idOrientador() shouldBe orientadorId
        salvo.membros.size shouldBe 2
        outbox.tipos shouldBe listOf("tcc.registrado")
        audit.tipos shouldBe listOf("tcc.registrado")
    }

    "aluno inexistente e fora do escopo devolvem a mesma ausência" {
        val alunos = mapOf(alunoId to outroCurso)
        val fora = shouldThrow<RecursoNaoEncontradoException> {
            registrar(MemoriaTccs(), MemoriaOutbox(), MemoriaAudit(), setOf(cursoId), alunos, setOf(orientadorId))
                .execute(atorId, alunoId, "Título", defesa, entrega, listOf(orientadorId to "ORIENTADOR"), null)
        }
        val ausente = shouldThrow<RecursoNaoEncontradoException> {
            registrar(MemoriaTccs(), MemoriaOutbox(), MemoriaAudit(), setOf(cursoId), alunos, setOf(orientadorId))
                .execute(
                    atorId,
                    UUID.fromString("01800000-0000-7000-8000-00000000c199"),
                    "Título",
                    defesa,
                    entrega,
                    listOf(orientadorId to "ORIENTADOR"),
                    null,
                )
        }
        fora.message shouldBe ausente.message
    }

    "membro inexistente é 422 e segundo ativo é 409" {
        shouldThrow<DadoInvalidoException> {
            registrar(
                MemoriaTccs(),
                MemoriaOutbox(),
                MemoriaAudit(),
                setOf(cursoId),
                mapOf(alunoId to cursoId),
                setOf(orientadorId),
            ).execute(
                atorId,
                alunoId,
                "Título válido",
                defesa,
                entrega,
                listOf(orientadorId to "ORIENTADOR", bancaId to "BANCA"),
                null,
            )
        }
        val repo = MemoriaTccs()
        registrar(repo, MemoriaOutbox(), MemoriaAudit(), setOf(cursoId), mapOf(alunoId to cursoId), setOf(orientadorId))
            .execute(atorId, alunoId, "Primeiro", defesa, entrega, listOf(orientadorId to "ORIENTADOR"), null)
        shouldThrow<ConflitoEstadoException> {
            registrar(repo, MemoriaOutbox(), MemoriaAudit(), setOf(cursoId), mapOf(alunoId to cursoId), setOf(orientadorId))
                .execute(atorId, alunoId, "Segundo", defesa, entrega, listOf(orientadorId to "ORIENTADOR"), null)
        }
    }

    "edição preserva id do membro e concluído não muda" {
        val membroId = UUID.fromString("01800000-0000-7000-8000-00000000c107")
        val aberto = Tcc.abrir(
            UUID.fromString("01800000-0000-7000-8000-00000000c108"),
            alunoId,
            cursoId,
            "Título",
            defesa,
            entrega,
            OffsetDateTime.parse("2026-09-26T12:00:00Z"),
            listOf(MembroBancaTcc(membroId, orientadorId, PapelBancaTcc.ORIENTADOR)),
        )
        val repo = MemoriaTccs(mutableListOf(aberto))
        val audit = MemoriaAudit()
        val atualizado = atualizar(repo, MemoriaOutbox(), audit, setOf(cursoId), setOf(orientadorId, bancaId))
            .execute(
                atorId,
                aberto.id,
                alunoId,
                "Título novo",
                defesa.plusDays(1),
                entrega,
                listOf(orientadorId to "ORIENTADOR", bancaId to "BANCA"),
                "10.0.0.1",
            )
        atualizado.titulo shouldBe "Título novo"
        atualizado.membros.find { it.idUsuario == orientadorId }!!.id shouldBe membroId
        atualizado.membros.size shouldBe 2
        audit.tipos shouldBe listOf("tcc.atualizado")

        val concluido = Tcc(
            UUID.fromString("01800000-0000-7000-8000-00000000c109"),
            alunoId,
            cursoId,
            "Concluído",
            TccSituacao.CONCLUIDO,
            TccEstado.APROVADO,
            defesa,
            entrega,
            null,
            null,
            null,
            null,
            null,
            OffsetDateTime.parse("2026-09-26T12:00:00Z"),
            OffsetDateTime.parse("2026-09-26T12:00:00Z"),
            listOf(MembroBancaTcc(membroId, orientadorId, PapelBancaTcc.ORIENTADOR)),
        )
        shouldThrow<ConflitoEstadoException> {
            atualizar(MemoriaTccs(mutableListOf(concluido)), MemoriaOutbox(), MemoriaAudit(), setOf(cursoId), setOf(orientadorId))
                .execute(
                    atorId,
                    concluido.id,
                    alunoId,
                    "Não",
                    defesa,
                    entrega,
                    listOf(orientadorId to "ORIENTADOR"),
                    null,
                )
        }
        concluido.titulo shouldBe "Concluído"
    }
})

private fun registrar(
    repo: TccRepository,
    outbox: OutboxPort,
    audit: AuditLogPort,
    cursos: Set<UUID>,
    alunos: Map<UUID, UUID>,
    usuarios: Set<UUID>,
) = RegistrarTccUseCase(
    repo,
    AlunosMemoria(alunos),
    CursosMemoria(cursos),
    UsuariosMemoria(usuarios),
    outbox,
    audit,
    ObjectMapper(),
)

private fun atualizar(
    repo: TccRepository,
    outbox: OutboxPort,
    audit: AuditLogPort,
    cursos: Set<UUID>,
    usuarios: Set<UUID>,
) = AtualizarTccUseCase(repo, CursosMemoria(cursos), UsuariosMemoria(usuarios), outbox, audit, ObjectMapper())

private class CursosMemoria(private val ids: Set<UUID>) : CursoEscopoPort {
    override fun cursoIdsDoUsuario(usuarioId: UUID): Set<UUID> = ids

    override fun cursoIdDoAlunoPorGrrOuEmail(grr: String?, emailInstitucional: String?): UUID? = null
}

private class AlunosMemoria(private val cursos: Map<UUID, UUID>) : AlunoTccPort {
    override fun resolver(usuarioId: UUID): AlunoTccPort.AlunoRef? = null

    override fun nomeDe(alunoId: UUID): String? = null

    override fun cadastro(alunoId: UUID): Cadastro? = cursos[alunoId]?.let { Cadastro(alunoId, it) }
}

private class UsuariosMemoria(private val ids: Set<UUID>) : UsuarioExistenciaPort {
    override fun existe(id: UUID): Boolean = id in ids

    override fun existemTodos(ids: Collection<UUID>): Boolean = ids.all { it in this.ids }
}

private class MemoriaTccs(
    private val itens: MutableList<Tcc> = mutableListOf(),
) : TccRepository {
    override fun save(tcc: Tcc): Tcc {
        itens.removeIf { it.id == tcc.id }
        itens += tcc
        return tcc
    }

    override fun findById(id: UUID): Tcc? = itens.find { it.id == id }

    override fun findByAluno(alunoId: UUID, estado: TccEstado?, pageable: Pageable): Page<Tcc> = Page.empty()

    override fun findParaRevisao(usuarioId: UUID, estado: TccEstado?, pageable: Pageable): Page<Tcc> = Page.empty()

    override fun findByCursos(cursoIds: Collection<UUID>, situacao: TccSituacao?, pageable: Pageable): Page<Tcc> =
        Page.empty()

    override fun existsAtivoByAluno(alunoId: UUID): Boolean =
        itens.any { it.idAluno == alunoId && it.situacao == TccSituacao.ATIVO }
}

private class MemoriaOutbox : OutboxPort {
    val tipos = mutableListOf<String>()

    override fun enqueue(tipo: String, payload: String) {
        tipos += tipo
    }

    override fun claimPending(limit: Int, staleBefore: OffsetDateTime) =
        emptyList<br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim>()

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
