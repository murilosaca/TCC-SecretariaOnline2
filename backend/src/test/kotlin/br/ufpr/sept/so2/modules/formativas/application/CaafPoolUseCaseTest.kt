package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoPorFormativaPort
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoResumoPort
import br.ufpr.sept.so2.modules.formativas.application.ports.AutorFormativaPort
import br.ufpr.sept.so2.modules.formativas.application.ports.ComissaoMembroPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import br.ufpr.sept.so2.modules.formativas.domain.TipoComissao
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
import java.time.OffsetDateTime
import java.util.UUID

class CaafPoolUseCaseTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-21T15:00:00Z")
    val cursoId = UUID.fromString("01800000-0000-7000-8000-00000000caa1")
    val outroCurso = UUID.fromString("01800000-0000-7000-8000-00000000caa2")
    val atorId = UUID.fromString("01800000-0000-7000-8000-00000000caa3")
    val colegaId = UUID.fromString("01800000-0000-7000-8000-00000000caa4")
    val alunoId = UUID.fromString("01800000-0000-7000-8000-00000000caa5")
    val eventoId = UUID.fromString("01800000-0000-7000-8000-00000000caa6")

    fun aguardando(): Formativa {
        val f = Formativa.viaPresenca(
            UUID.fromString("01800000-0000-7000-8000-00000000caa7"),
            alunoId,
            eventoId,
            "Pool CAAF",
            4,
            agora,
        )
        f.confirmar(agora)
        return f
    }

    "pool vazio quando o professor não é membro da CAAF" {
        val visao = ObterPoolCaafUseCase(FakeFormativas(), FakeMembros(), FakeAutor()).execute(atorId)
        visao.itens.size shouldBe 0
        visao.kpis.poolTotal shouldBe 0
        visao.membros.size shouldBe 0
    }

    "self-assign notifica só o colega e não o ator" {
        val formativa = aguardando()
        val repo = FakeFormativas(mutableListOf(formativa), mapOf(alunoId to cursoId))
        val membros = FakeMembros(mapOf(atorId to setOf(cursoId)))
        val outbox = FakeOutbox()
        val audit = FakeAudit()
        val alunos = FakeAlunos(mapOf(alunoId to cursoId))
        val obter = ObterPoolCaafUseCase(repo, membros, FakeAutor())
        val visao = AtribuirFormativaCaafUseCase(
            repo,
            membros,
            alunos,
            obter,
            outbox,
            audit,
            ObjectMapper(),
        ).execute(atorId, formativa.id, atorId, "127.0.0.1")
        visao.itens.single().atribuidaA(atorId) shouldBe true
        visao.kpis.atribuidasAMim shouldBe 1
        visao.kpis.poolTotal shouldBe 0
        outbox.tipos shouldBe listOf("formativas.assigned")
        outbox.payloads.single().contains("\"notificarResponsavel\":false") shouldBe true
        audit.tipos shouldBe listOf("formativas.assigned")
    }

    "atribuir a colega notifica o novo responsável" {
        val formativa = aguardando()
        val repo = FakeFormativas(mutableListOf(formativa), mapOf(alunoId to cursoId))
        val membros = FakeMembros(mapOf(atorId to setOf(cursoId), colegaId to setOf(cursoId)))
        val outbox = FakeOutbox()
        AtribuirFormativaCaafUseCase(
            repo,
            membros,
            FakeAlunos(mapOf(alunoId to cursoId)),
            ObterPoolCaafUseCase(repo, membros, FakeAutor()),
            outbox,
            FakeAudit(),
            ObjectMapper(),
        ).execute(atorId, formativa.id, colegaId, null)
        formativa.atribuidaA(colegaId) shouldBe true
        outbox.payloads.single().contains("\"notificarResponsavel\":true") shouldBe true
    }

    "formativa de outro curso não é enumerada" {
        val formativa = aguardando()
        val repo = FakeFormativas(mutableListOf(formativa), mapOf(alunoId to outroCurso))
        val membros = FakeMembros(mapOf(atorId to setOf(cursoId)))
        shouldThrow<RecursoNaoEncontradoException> {
            AtribuirFormativaCaafUseCase(
                repo,
                membros,
                FakeAlunos(mapOf(alunoId to outroCurso)),
                ObterPoolCaafUseCase(repo, membros, FakeAutor()),
                FakeOutbox(),
                FakeAudit(),
                ObjectMapper(),
            ).execute(atorId, formativa.id, atorId, null)
        }
    }

    "lote aprova só presença validada e rejeita indeferir" {
        val formativa = aguardando()
        val repo = FakeFormativas(mutableListOf(formativa), mapOf(alunoId to cursoId))
        val membros = FakeMembros(mapOf(atorId to setOf(cursoId)))
        val outbox = FakeOutbox()
        val certificados = FakeCertificados()
        val obter = ObterPoolCaafUseCase(repo, membros, FakeAutor())
        val batch = BatchAprovarFormativasCaafUseCase(
            repo,
            membros,
            FakeAlunos(mapOf(alunoId to cursoId)),
            obter,
            certificados,
            outbox,
            FakeAudit(),
            ObjectMapper(),
        )
        shouldThrow<DadoInvalidoException> {
            batch.execute(atorId, listOf(formativa.id), "INDEFERIDA", null)
        }
        val visao = batch.execute(atorId, listOf(formativa.id), "APROVADA", "127.0.0.1")
        formativa.estado shouldBe FormativaEstado.APROVADA
        visao.itens.size shouldBe 0
        certificados.emitidas shouldBe 1
        outbox.tipos shouldBe listOf("formativa.aprovada", "formativas.batch_approved")
    }
})

private class FakeFormativas(
    private val itens: MutableList<Formativa> = mutableListOf(),
    private val cursoPorAluno: Map<UUID, UUID> = emptyMap(),
) : FormativaRepository {
    override fun save(formativa: Formativa): Formativa {
        itens.removeIf { it.id == formativa.id }
        itens.add(formativa)
        return formativa
    }

    override fun findById(id: UUID): Formativa? = itens.find { it.id == id }

    override fun findByEventoAndAluno(eventoId: UUID, alunoId: UUID): Formativa? =
        itens.find { it.idEvento == eventoId && it.idAluno == alunoId }

    override fun findByAluno(alunoId: UUID, pageable: Pageable): Page<Formativa> =
        PageImpl(itens.filter { it.idAluno == alunoId })

    override fun findByEstado(estado: FormativaEstado, pageable: Pageable): Page<Formativa> =
        PageImpl(itens.filter { it.estado == estado })

    override fun somarCargaHoraria(alunoId: UUID, estado: FormativaEstado): Int =
        itens.filter { it.idAluno == alunoId && it.estado == estado }.sumOf { it.cargaHoraria }

    override fun findPendentesConfirmacao(alunoId: UUID, limite: Int): List<Formativa> =
        itens.filter { it.idAluno == alunoId && it.estado == FormativaEstado.PENDENTE_CONFIRMACAO }.take(limite)

    override fun findPoolCaaf(cursoIds: Collection<UUID>, usuarioId: UUID): List<Formativa> =
        itens.filter {
            cursoPorAluno[it.idAluno] in cursoIds &&
                it.estado == FormativaEstado.AGUARDANDO_CAAF &&
                (it.idResponsavel == null || it.idResponsavel == usuarioId)
        }

    override fun countSemResponsavel(cursoIds: Collection<UUID>): Long =
        itens.count {
            cursoPorAluno[it.idAluno] in cursoIds &&
                it.estado == FormativaEstado.AGUARDANDO_CAAF &&
                it.idResponsavel == null
        }.toLong()

    override fun countAtribuidasAguardando(cursoIds: Collection<UUID>, responsavelId: UUID): Long =
        itens.count {
            cursoPorAluno[it.idAluno] in cursoIds &&
                it.estado == FormativaEstado.AGUARDANDO_CAAF &&
                it.idResponsavel == responsavelId
        }.toLong()

    override fun countAprovadasDesde(cursoIds: Collection<UUID>, inicio: OffsetDateTime): Long =
        itens.count {
            cursoPorAluno[it.idAluno] in cursoIds &&
                it.estado == FormativaEstado.APROVADA &&
                (it.reviewedAt?.isAfter(inicio) == true || it.reviewedAt == inicio)
        }.toLong()

    override fun countCargaAguardando(
        cursoIds: Collection<UUID>,
        responsavelIds: Collection<UUID>,
    ): Map<UUID, Long> =
        itens
            .filter {
                cursoPorAluno[it.idAluno] in cursoIds &&
                    it.estado == FormativaEstado.AGUARDANDO_CAAF &&
                    it.idResponsavel in responsavelIds
            }
            .groupingBy { it.idResponsavel!! }
            .eachCount()
            .mapValues { it.value.toLong() }
}

private class FakeMembros(
    private val cursos: Map<UUID, Set<UUID>> = emptyMap(),
) : ComissaoMembroPort {
    override fun cursosDoMembro(usuarioId: UUID, tipo: TipoComissao): Set<UUID> =
        if (tipo == TipoComissao.CAAF) cursos[usuarioId].orEmpty() else emptySet()

    override fun membrosDosCursos(cursoIds: Collection<UUID>, tipo: TipoComissao): List<UUID> =
        cursos.filter { (_, cs) -> cs.any { it in cursoIds } }.keys.toList()

    override fun ehMembro(usuarioId: UUID, cursoId: UUID, tipo: TipoComissao): Boolean =
        tipo == TipoComissao.CAAF && cursos[usuarioId]?.contains(cursoId) == true

    override fun adicionarSeAusente(cursoId: UUID, usuarioId: UUID, tipo: TipoComissao) = Unit
}

private class FakeAlunos(private val cursoPorAluno: Map<UUID, UUID>) : AlunoResumoPort {
    override fun nomeDe(alunoId: UUID): String? = "Aluno"
    override fun cursoDe(alunoId: UUID): UUID? = cursoPorAluno[alunoId]
}

private class FakeAutor : AutorFormativaPort {
    override fun rotulo(usuarioId: UUID): String? = usuarioId.toString()
}

private class FakeOutbox : OutboxPort {
    val tipos = mutableListOf<String>()
    val payloads = mutableListOf<String>()
    override fun enqueue(tipo: String, payload: String) {
        tipos.add(tipo)
        payloads.add(payload)
    }
    override fun claimPending(limit: Int, staleBefore: OffsetDateTime) =
        emptyList<br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim>()
    override fun markSent(id: UUID) = Unit
    override fun markFailed(id: UUID, tentativas: Int, lastError: String?) = Unit
    override fun markPendingRetry(id: UUID, tentativas: Int, lastError: String?) = Unit
}

private class FakeAudit : AuditLogPort {
    val tipos = mutableListOf<String>()
    override fun append(tipo: String, atorId: UUID?, payload: String?, ip: String?) {
        tipos.add(tipo)
    }
}

private class FakeCertificados : CertificadoPorFormativaPort {
    var emitidas = 0
    override fun emitirSeAusente(
        formativaId: UUID,
        alunoId: UUID,
        eventoId: UUID?,
        titulo: String,
        cargaHoraria: Int,
        atorId: UUID,
        ip: String?,
    ) {
        emitidas++
    }
}
