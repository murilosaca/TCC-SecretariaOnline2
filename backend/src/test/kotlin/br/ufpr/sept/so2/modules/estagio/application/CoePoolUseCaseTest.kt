package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.application.ports.AutorEstagioPort
import br.ufpr.sept.so2.modules.estagio.application.ports.CoeMembroPort
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.DocumentoEstagio
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.EstagioSituacao
import br.ufpr.sept.so2.modules.estagio.domain.TipoDocumentoEstagio
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class CoePoolUseCaseTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-21T15:00:00Z")
    val cursoId = UUID.fromString("01800000-0000-7000-8000-00000000c0e1")
    val outroCurso = UUID.fromString("01800000-0000-7000-8000-00000000c0e2")
    val atorId = UUID.fromString("01800000-0000-7000-8000-00000000c0e3")
    val colegaId = UUID.fromString("01800000-0000-7000-8000-00000000c0e4")
    val alunoId = UUID.fromString("01800000-0000-7000-8000-00000000c0e5")

    fun semOrientador(): Estagio = Estagio.abrir(
        UUID.fromString("01800000-0000-7000-8000-00000000c0e6"),
        alunoId,
        cursoId,
        null,
        "Pool COE SEPT",
        "Ana",
        LocalDate.parse("2026-04-01"),
        LocalDate.parse("2026-12-15"),
        agora,
        listOf(DocumentoEstagio.pendente(UUID.randomUUID(), TipoDocumentoEstagio.TCE)),
    )

    "pool vazio quando o professor não é membro do COE" {
        val visao = ObterPoolCoeUseCase(FakeEstagios(), FakeMembros(), FakeAutor()).execute(atorId)
        visao.itens.size shouldBe 0
        visao.kpis.poolTotal shouldBe 0
        visao.membros.size shouldBe 0
    }

    "self-assign notifica o aluno e não o ator" {
        val estagio = semOrientador()
        val repo = FakeEstagios(mutableListOf(estagio))
        val membros = FakeMembros(mapOf(atorId to setOf(cursoId)))
        val outbox = FakeOutbox()
        val audit = FakeAudit()
        val obter = ObterPoolCoeUseCase(repo, membros, FakeAutor())
        val visao = AtribuirEstagioCoeUseCase(
            repo,
            membros,
            obter,
            outbox,
            audit,
            ObjectMapper(),
        ).execute(atorId, estagio.id, atorId, "127.0.0.1")
        visao.itens.single().orientadoPor(atorId) shouldBe true
        visao.kpis.atribuidosAMim shouldBe 1
        visao.kpis.poolTotal shouldBe 0
        outbox.tipos shouldBe listOf("estagio.orientador_atribuido")
        outbox.payloads.single().contains("\"notificarAluno\":true") shouldBe true
        outbox.payloads.single().contains("\"notificarOrientador\":false") shouldBe true
        audit.tipos shouldBe listOf("estagio.orientador_atribuido")
    }

    "atribuir a colega notifica o novo orientador" {
        val estagio = semOrientador()
        val repo = FakeEstagios(mutableListOf(estagio))
        val membros = FakeMembros(mapOf(atorId to setOf(cursoId), colegaId to setOf(cursoId)))
        val outbox = FakeOutbox()
        AtribuirEstagioCoeUseCase(
            repo,
            membros,
            ObterPoolCoeUseCase(repo, membros, FakeAutor()),
            outbox,
            FakeAudit(),
            ObjectMapper(),
        ).execute(atorId, estagio.id, colegaId, null)
        estagio.orientadoPor(colegaId) shouldBe true
        outbox.payloads.single().contains("\"notificarOrientador\":true") shouldBe true
    }

    "estágio de outro curso não é enumerado" {
        val estagio = Estagio.abrir(
            UUID.fromString("01800000-0000-7000-8000-00000000c0e7"),
            alunoId,
            outroCurso,
            null,
            "Fora",
            "Ana",
            LocalDate.parse("2026-04-01"),
            LocalDate.parse("2026-12-15"),
            agora,
            listOf(DocumentoEstagio.pendente(UUID.randomUUID(), TipoDocumentoEstagio.TCE)),
        )
        val repo = FakeEstagios(mutableListOf(estagio))
        val membros = FakeMembros(mapOf(atorId to setOf(cursoId)))
        shouldThrow<RecursoNaoEncontradoException> {
            AtribuirEstagioCoeUseCase(
                repo,
                membros,
                ObterPoolCoeUseCase(repo, membros, FakeAutor()),
                FakeOutbox(),
                FakeAudit(),
                ObjectMapper(),
            ).execute(atorId, estagio.id, atorId, null)
        }
    }

    "destinatário fora do COE é inválido" {
        val estagio = semOrientador()
        val repo = FakeEstagios(mutableListOf(estagio))
        val membros = FakeMembros(mapOf(atorId to setOf(cursoId)))
        shouldThrow<DadoInvalidoException> {
            AtribuirEstagioCoeUseCase(
                repo,
                membros,
                ObterPoolCoeUseCase(repo, membros, FakeAutor()),
                FakeOutbox(),
                FakeAudit(),
                ObjectMapper(),
            ).execute(atorId, estagio.id, colegaId, null)
        }
    }
})

private class FakeEstagios(
    private val itens: MutableList<Estagio> = mutableListOf(),
) : EstagioRepository {
    override fun save(estagio: Estagio): Estagio {
        itens.removeIf { it.id == estagio.id }
        itens += estagio
        return estagio
    }

    override fun findById(id: UUID): Estagio? = itens.find { it.id == id }

    override fun findByAluno(alunoId: UUID, situacao: EstagioSituacao?, pageable: Pageable): Page<Estagio> =
        Page.empty()

    override fun findParaRevisao(orientadorId: UUID, situacao: EstagioSituacao?, pageable: Pageable): Page<Estagio> =
        Page.empty()

    override fun findByCursos(cursoIds: Collection<UUID>, situacao: EstagioSituacao?, pageable: Pageable): Page<Estagio> {
        val filtrados = itens.filter { it.idCurso in cursoIds && (situacao == null || it.situacao == situacao) }
        return PageImpl(filtrados, pageable, filtrados.size.toLong())
    }

    override fun findPoolCoe(cursoIds: Collection<UUID>, usuarioId: UUID): List<Estagio> =
        itens.filter { it.idCurso in cursoIds && it.situacao != EstagioSituacao.CONCLUIDO && it.podeAtribuir(usuarioId) }

    override fun countSemOrientador(cursoIds: Collection<UUID>): Long =
        itens.count { it.idCurso in cursoIds && it.semOrientador() && it.situacao != EstagioSituacao.CONCLUIDO }.toLong()

    override fun countAtribuidosAtivos(cursoIds: Collection<UUID>, orientadorId: UUID): Long =
        itens.count { it.idCurso in cursoIds && it.orientadoPor(orientadorId) && it.situacao != EstagioSituacao.CONCLUIDO }
            .toLong()

    override fun countConcluidosDesde(cursoIds: Collection<UUID>, inicio: OffsetDateTime): Long = 0

    override fun countCargaAtiva(cursoIds: Collection<UUID>, orientadorIds: Collection<UUID>): Map<UUID, Long> =
        orientadorIds.associateWith { id -> countAtribuidosAtivos(cursoIds, id) }

    override fun existsByAluno(alunoId: UUID): Boolean = itens.any { it.idAluno == alunoId }

    override fun existsSemOrientador(cursoId: UUID): Boolean =
        itens.any { it.idCurso == cursoId && it.semOrientador() }
}

private class FakeMembros(
    private val vinculos: Map<UUID, Set<UUID>> = emptyMap(),
) : CoeMembroPort {
    override fun cursosDoMembro(usuarioId: UUID): Set<UUID> = vinculos[usuarioId].orEmpty()

    override fun membrosDosCursos(cursoIds: Collection<UUID>): List<UUID> =
        vinculos.filterValues { cursos -> cursos.any { it in cursoIds } }.keys.toList()

    override fun ehMembro(usuarioId: UUID, cursoId: UUID): Boolean =
        cursoId in vinculos[usuarioId].orEmpty()

    override fun adicionarSeAusente(cursoId: UUID, usuarioId: UUID) = Unit
}

private class FakeAutor : AutorEstagioPort {
    override fun rotulo(usuarioId: UUID): String = "$usuarioId@ufpr.br"
}

private class FakeOutbox : OutboxPort {
    val tipos = mutableListOf<String>()
    val payloads = mutableListOf<String>()
    override fun enqueue(tipo: String, payload: String) {
        tipos += tipo
        payloads += payload
    }
    override fun claimPending(limit: Int, staleBefore: OffsetDateTime) = emptyList<br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim>()
    override fun markSent(id: UUID) = Unit
    override fun markFailed(id: UUID, tentativas: Int, lastError: String?) = Unit
    override fun markPendingRetry(id: UUID, tentativas: Int, lastError: String?) = Unit
}

private class FakeAudit : AuditLogPort {
    val tipos = mutableListOf<String>()
    override fun append(tipo: String, atorId: UUID?, payload: String?, ip: String?) {
        tipos += tipo
    }
}
