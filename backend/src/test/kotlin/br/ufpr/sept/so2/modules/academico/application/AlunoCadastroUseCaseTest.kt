package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.ContaAlunoPort
import br.ufpr.sept.so2.modules.academico.application.ports.ContaProvisionada
import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.ProvisionarContaAluno
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

class AlunoCadastroUseCaseTest : StringSpec({
    "criar grava a conta vinculada e o audit sem senha nem GRR completo" {
        val cursoId = UUID.randomUUID()
        val operador = UUID.randomUUID()
        val usuario = UUID.randomUUID()
        val audit = FakeAuditAluno()
        val contas = FakeContas(usuario)
        val service = service(cursoId, operador, audit, contas)
        val aluno = service.criar(
            operador,
            "Ana",
            null,
            "GRR20240021",
            "ana.nova@ufpr.br",
            null,
            null,
            cursoId,
            AlunoSituacao.MATRICULADO,
            "127.0.0.1",
        )
        aluno.idUsuario shouldBe usuario
        contas.comandos.single().grr shouldBe "GRR20240021"
        audit.tipos shouldBe listOf("aluno.criado")
        audit.payloads.single().shouldNotContain("senha")
        audit.payloads.single().shouldNotContain("GRR20240021")
        audit.payloads.single().shouldContain(usuario.toString())
    }

    "editar audita só o campo que mudou" {
        val cursoId = UUID.randomUUID()
        val operador = UUID.randomUUID()
        val agora = OffsetDateTime.parse("2026-11-08T12:00:00Z")
        val aluno = Aluno(
            UUID.randomUUID(),
            "Ana",
            null,
            Grr.of("GRR20240021"),
            Email.of("ana.nova@ufpr.br"),
            null,
            "41999990000",
            cursoId,
            AlunoSituacao.MATRICULADO,
            true,
            agora,
            agora,
            UUID.randomUUID(),
        )
        val audit = FakeAuditAluno()
        val service = service(cursoId, operador, audit, FakeContas(UUID.randomUUID()), aluno)
        service.atualizar(aluno.id, operador, null, null, null, "4133330000", null, null, null, "127.0.0.1")
        audit.tipos shouldBe listOf("aluno.atualizado")
        val payload = audit.payloads.single()
        payload.shouldContain("\"campo\":\"telefone\"")
        payload.shouldContain("\"de\":\"41999990000\"")
        payload.shouldContain("\"para\":\"4133330000\"")
        payload.shouldNotContain("\"campo\":\"nome\"")
        payload.shouldNotContain("senha")
        payload.shouldNotContain("GRR20240021")
    }
})

private fun service(
    cursoId: UUID,
    operador: UUID,
    audit: FakeAuditAluno,
    contas: FakeContas,
    existente: Aluno? = null,
) = AlunoApplicationService(
    FakeAlunoRepo(existente),
    FakeCursoRepo(cursoId),
    EscopoDoOperador(operador, cursoId),
    contas,
    audit,
    ObjectMapper(),
)

private class EscopoDoOperador(private val operador: UUID, private val cursoId: UUID) : CursoEscopoPort {
    override fun cursoIdsDoUsuario(usuarioId: UUID): Set<UUID> =
        if (usuarioId == operador) setOf(cursoId) else emptySet()

    override fun cursoIdDoAlunoPorGrrOuEmail(grr: String?, emailInstitucional: String?): UUID? = null
}

private class FakeContas(private val usuarioId: UUID) : ContaAlunoPort {
    val comandos = mutableListOf<ProvisionarContaAluno>()
    override fun provisionar(comando: ProvisionarContaAluno): ContaProvisionada {
        comandos += comando
        return ContaProvisionada(usuarioId, vinculada = false)
    }
    override fun localizar(grr: String, emailInstitucional: String): UUID? = null
    override fun resetarSenha(operadorId: UUID, usuarioId: UUID, ip: String?) = Unit
}

private class FakeAuditAluno : AuditLogPort {
    val tipos = mutableListOf<String>()
    val payloads = mutableListOf<String>()
    override fun append(tipo: String, atorId: UUID?, payload: String?, ip: String?) {
        tipos += tipo
        payloads += payload.orEmpty()
    }
}

private class FakeAlunoRepo(inicial: Aluno?) : AlunoRepository {
    private val alunos = mutableMapOf<UUID, Aluno>()

    init {
        if (inicial != null) {
            alunos[inicial.id] = inicial
        }
    }

    override fun save(aluno: Aluno): Aluno {
        alunos[aluno.id] = aluno
        return aluno
    }
    override fun findById(id: UUID): Optional<Aluno> = Optional.ofNullable(alunos[id])
    override fun findByGrr(grr: String): Optional<Aluno> = Optional.empty()
    override fun findByEmailInstitucional(email: String): Optional<Aluno> = Optional.empty()
    override fun findByIdUsuario(idUsuario: UUID): Optional<Aluno> = Optional.empty()
    override fun findAll(idCurso: UUID?, termo: String?, cursoIds: Collection<UUID>?, pageable: Pageable): Page<Aluno> =
        Page.empty()
    override fun findByIdCursoIn(cursoIds: Collection<UUID>): List<Aluno> = emptyList()
    override fun existsByGrr(grr: String): Boolean = false
    override fun existsByEmailInstitucional(email: String): Boolean = false
    override fun deleteById(id: UUID) {
        alunos.remove(id)
    }
}

private class FakeCursoRepo(private val cursoId: UUID) : CursoRepository {
    override fun save(curso: Curso): Curso = curso
    override fun findById(id: UUID): Optional<Curso> =
        if (id == cursoId) {
            Optional.of(Curso(cursoId, "TADS", "TADS", "TADS", null, 120, true, OffsetDateTime.now(), OffsetDateTime.now()))
        } else {
            Optional.empty()
        }
    override fun findByIdForUpdate(id: UUID): Optional<Curso> = findById(id)
    override fun findAll(pageable: Pageable): Page<Curso> = Page.empty()
    override fun existsBySigla(sigla: String): Boolean = false
    override fun existsByCodigo(codigo: String): Boolean = false
    override fun findByCodigo(codigo: String): Optional<Curso> = Optional.empty()
    override fun findBySigla(sigla: String): Optional<Curso> = Optional.empty()
    override fun findAllByIds(ids: Collection<UUID>, pageable: Pageable): Page<Curso> = Page.empty()
    override fun findIdsByCoordenador(idCoordenador: UUID): Set<UUID> = emptySet()
    override fun findAtivos(): List<Curso> = emptyList()
    override fun deleteById(id: UUID) = Unit
}
