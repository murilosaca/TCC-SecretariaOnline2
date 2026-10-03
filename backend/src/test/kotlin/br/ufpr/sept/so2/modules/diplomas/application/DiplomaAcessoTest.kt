package br.ufpr.sept.so2.modules.diplomas.application

import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.modules.diplomas.domain.DiplomaSituacao
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.UUID

class DiplomaAcessoTest : StringSpec({
    "exige ao menos um curso vinculado" {
        shouldThrow<AcessoNegadoException> {
            DiplomaAcesso.exigirEscopo(emptySet())
        }.message shouldBe DiplomaAcesso.MSG_SEM_CURSO
        val curso = UUID.fromString("01999999-0000-7000-8000-00000000f516")
        shouldThrow<AcessoNegadoException> {
            DiplomaAcesso.exigirCursoNoEscopo(curso, emptySet())
        }.message shouldBe DiplomaAcesso.MSG_SEM_CURSO
    }

    "bloqueia curso explicito fora do escopo" {
        val meu = UUID.fromString("01999999-0000-7000-8000-00000000f518")
        val outro = UUID.fromString("01999999-0000-7000-8000-00000000f519")
        shouldThrow<AcessoNegadoException> {
            DiplomaAcesso.exigirCursoNoEscopo(outro, setOf(meu))
        }.message shouldBe DiplomaAcesso.MSG_FORA_ESCOPO
        DiplomaAcesso.exigirCursoNoEscopo(meu, setOf(meu)) shouldBe meu
    }

    "diploma por id fora do escopo ou inexistente devolve a mesma mensagem" {
        val meu = UUID.fromString("01999999-0000-7000-8000-00000000f518")
        val outro = UUID.fromString("01999999-0000-7000-8000-00000000f519")
        val diploma = Diploma.registrar(
            UUID.fromString("01999999-0000-7000-8000-00000000f520"),
            UUID.fromString("01999999-0000-7000-8000-00000000f521"),
            meu,
            null,
            "L12-F3-001",
            OffsetDateTime.parse("2026-11-15T15:00:00Z"),
            "12",
            "3",
            null,
            OffsetDateTime.parse("2026-11-15T15:00:00Z"),
        )
        val repositorio = DiplomasMemoria(mapOf(diploma.id to diploma))
        val inexistente = UUID.fromString("01999999-0000-7000-8000-00000000f522")

        val fora = shouldThrow<RecursoNaoEncontradoException> {
            DiplomaAcesso.exigirDiploma(setOf(outro), repositorio, diploma.id)
        }
        val sumido = shouldThrow<RecursoNaoEncontradoException> {
            DiplomaAcesso.exigirDiploma(setOf(meu), repositorio, inexistente)
        }
        val semCurso = shouldThrow<RecursoNaoEncontradoException> {
            DiplomaAcesso.exigirDiploma(emptySet(), repositorio, diploma.id)
        }
        fora.message shouldBe DiplomaAcesso.MSG_NAO_ENCONTRADO
        sumido.message shouldBe fora.message
        semCurso.message shouldBe fora.message
        DiplomaAcesso.exigirDiploma(setOf(meu), repositorio, diploma.id).id shouldBe diploma.id
    }
})

private class DiplomasMemoria(private val itens: Map<UUID, Diploma>) : DiplomaRepository {
    override fun save(diploma: Diploma): Diploma = diploma

    override fun findById(id: UUID): Diploma? = itens[id]

    override fun findByAluno(alunoId: UUID): Diploma? = itens.values.find { it.idAluno == alunoId }

    override fun existsByAluno(alunoId: UUID): Boolean = findByAluno(alunoId) != null

    override fun findByCurso(
        cursoId: UUID,
        situacao: DiplomaSituacao?,
        pageable: Pageable,
    ): Page<Diploma> = Page.empty(pageable)

    override fun findByCursos(
        cursoIds: Collection<UUID>,
        ano: Int?,
        situacao: DiplomaSituacao?,
        pageable: Pageable,
    ): Page<Diploma> = Page.empty(pageable)
}
