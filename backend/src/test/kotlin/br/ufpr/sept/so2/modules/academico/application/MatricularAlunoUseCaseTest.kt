package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.DisciplinaRepository
import br.ufpr.sept.so2.modules.academico.application.ports.MatriculaRepository
import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Disciplina
import br.ufpr.sept.so2.modules.academico.domain.Matricula
import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.domain.exception.SemVagasException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

class MatricularAlunoUseCaseTest : StringSpec({
    val cursoId = UUID.randomUUID()
    val operadorId = UUID.randomUUID()
    val agora = OffsetDateTime.parse("2026-11-08T12:00:00Z")
    val aluno = aluno(cursoId, agora)
    val periodo = PeriodoLetivo(
        UUID.randomUUID(),
        2026,
        2,
        LocalDate.parse("2026-08-01"),
        LocalDate.parse("2026-12-15"),
        true,
        agora,
        agora,
    )

    "lote com uma disciplina sem vaga não grava nenhuma matrícula" {
        val comVaga = disciplina(cursoId, "BDD01", 2, agora)
        val semVaga = disciplina(cursoId, "WEB01", 0, agora)
        val matriculas = FakeMatriculas()
        val disciplinas = FakeDisciplinas(listOf(comVaga, semVaga))
        val useCase = useCase(aluno, operadorId, cursoId, periodo, disciplinas, matriculas)
        shouldThrow<SemVagasException> {
            useCase.execute(aluno.id, operadorId, listOf(comVaga.id, semVaga.id), "127.0.0.1")
        }.disciplinaId shouldBe semVaga.id
        matriculas.salvas shouldHaveSize 0
        comVaga.vagas shouldBe 2
    }

    "lote válido decrementa vaga, grava matrícula e audita sem outbox" {
        val disciplina = disciplina(cursoId, "BDD01", 2, agora)
        val matriculas = FakeMatriculas()
        val audit = FakeAuditMatricula()
        val useCase = useCase(aluno, operadorId, cursoId, periodo, FakeDisciplinas(listOf(disciplina)), matriculas, audit)
        val resultado = useCase.execute(aluno.id, operadorId, listOf(disciplina.id), "127.0.0.1")
        resultado.periodoId shouldBe periodo.id
        resultado.disciplinaIds shouldBe listOf(disciplina.id)
        disciplina.vagas shouldBe 1
        matriculas.salvas shouldHaveSize 1
        audit.tipos shouldBe listOf("aluno.matriculado")
        audit.payloads.single().contains("senha") shouldBe false
    }

    "aluno de outro curso responde 404" {
        val useCase = useCase(aluno, operadorId, UUID.randomUUID(), periodo, FakeDisciplinas(emptyList()), FakeMatriculas())
        shouldThrow<RecursoNaoEncontradoException> {
            useCase.execute(aluno.id, operadorId, listOf(UUID.randomUUID()), null)
        }
    }
})

private fun useCase(
    aluno: Aluno,
    operadorId: UUID,
    cursoNoEscopo: UUID,
    periodo: PeriodoLetivo,
    disciplinas: FakeDisciplinas,
    matriculas: FakeMatriculas,
    audit: AuditLogPort = FakeAuditMatricula(),
) = MatricularAlunoUseCase(
    FakeAlunos(aluno),
    EscopoFixo(operadorId, cursoNoEscopo),
    disciplinas,
    FakePeriodos(periodo),
    matriculas,
    audit,
    ObjectMapper(),
)

private fun aluno(cursoId: UUID, agora: OffsetDateTime) = Aluno(
    UUID.randomUUID(),
    "Ana",
    null,
    Grr.of("GRR20240011"),
    Email.of("ana@ufpr.br"),
    null,
    null,
    cursoId,
    AlunoSituacao.MATRICULADO,
    true,
    agora,
    agora,
)

private fun disciplina(cursoId: UUID, codigo: String, vagas: Int, agora: OffsetDateTime) = Disciplina(
    UUID.randomUUID(),
    cursoId,
    codigo,
    "Disciplina $codigo",
    1,
    60,
    4,
    true,
    agora,
    agora,
    vagas,
)

private class FakeAlunos(private val aluno: Aluno) : AlunoRepository {
    override fun save(aluno: Aluno): Aluno = aluno
    override fun findById(id: UUID): Optional<Aluno> =
        if (id == aluno.id) Optional.of(aluno) else Optional.empty()
    override fun findByGrr(grr: String): Optional<Aluno> = Optional.empty()
    override fun findByEmailInstitucional(email: String): Optional<Aluno> = Optional.empty()
    override fun findByIdUsuario(idUsuario: UUID): Optional<Aluno> = Optional.empty()
    override fun findAll(idCurso: UUID?, termo: String?, cursoIds: Collection<UUID>?, pageable: Pageable): Page<Aluno> =
        Page.empty()
    override fun findByIdCursoIn(cursoIds: Collection<UUID>): List<Aluno> = emptyList()
    override fun existsByGrr(grr: String): Boolean = false
    override fun existsByEmailInstitucional(email: String): Boolean = false
    override fun deleteById(id: UUID) = Unit
}

private class FakeDisciplinas(items: List<Disciplina>) : DisciplinaRepository {
    private val items = items.associateBy { it.id }.toMutableMap()
    override fun save(disciplina: Disciplina): Disciplina {
        items[disciplina.id] = disciplina
        return disciplina
    }
    override fun findById(id: UUID): Disciplina? = items[id]
    override fun findAll(idCurso: UUID?, cursoIds: Collection<UUID>?, pageable: Pageable): Page<Disciplina> =
        PageImpl(items.values.filter { idCurso == null || it.idCurso == idCurso })
    override fun existsByCursoAndCodigo(idCurso: UUID, codigo: String): Boolean = false
    override fun findByCursoAndCodigo(idCurso: UUID, codigo: String): Disciplina? = null
    override fun lockByIds(ids: Collection<UUID>): List<Disciplina> = ids.mapNotNull { items[it] }
    override fun deleteById(id: UUID) {
        items.remove(id)
    }
}

private class FakePeriodos(private val vigente: PeriodoLetivo) : PeriodoLetivoRepository {
    override fun save(periodo: PeriodoLetivo): PeriodoLetivo = periodo
    override fun findById(id: UUID): Optional<PeriodoLetivo> = Optional.empty()
    override fun findAll(pageable: Pageable): Page<PeriodoLetivo> = Page.empty()
    override fun findAll(): List<PeriodoLetivo> = listOf(vigente)
    override fun findVigente(data: LocalDate): Optional<PeriodoLetivo> = Optional.of(vigente)
    override fun existsByAnoAndSemestre(ano: Int, semestre: Int): Boolean = false
    override fun deleteById(id: UUID) = Unit
}

private class FakeMatriculas : MatriculaRepository {
    val salvas = mutableListOf<Matricula>()
    override fun save(matricula: Matricula): Matricula {
        salvas += matricula
        return matricula
    }
    override fun exists(idAluno: UUID, idDisciplina: UUID, idPeriodoLetivo: UUID): Boolean = false
}

private class EscopoFixo(private val usuarioId: UUID, private val cursoId: UUID) : CursoEscopoPort {
    override fun cursoIdsDoUsuario(usuarioId: UUID): Set<UUID> =
        if (usuarioId == this.usuarioId) setOf(cursoId) else emptySet()

    override fun cursoIdDoAlunoPorGrrOuEmail(grr: String?, emailInstitucional: String?): UUID? = null
}

private class FakeAuditMatricula : AuditLogPort {
    val tipos = mutableListOf<String>()
    val payloads = mutableListOf<String>()
    override fun append(tipo: String, atorId: UUID?, payload: String?, ip: String?) {
        tipos += tipo
        payloads += payload.orEmpty()
    }
}
