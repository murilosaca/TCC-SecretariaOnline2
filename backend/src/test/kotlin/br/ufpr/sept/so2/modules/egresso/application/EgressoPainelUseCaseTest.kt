package br.ufpr.sept.so2.modules.egresso.application

import br.ufpr.sept.so2.modules.egresso.api.EgressoAssembler
import br.ufpr.sept.so2.modules.egresso.application.ports.EgressoConsultaPort
import br.ufpr.sept.so2.modules.egresso.application.ports.EgressoConsultaPort.Cadastro
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.UUID

class EgressoPainelUseCaseTest : StringSpec({
    val usuarioId = UUID.randomUUID()
    val alunoId = UUID.randomUUID()
    val certificadoId = UUID.randomUUID()
    val pdf = "pdf-original".toByteArray()
    val hash = "ab".repeat(32)
    val agora = OffsetDateTime.parse("2026-06-01T12:00:00Z")
    val cadastro = Cadastro(
        alunoId,
        true,
        "Egresso Dev",
        "Análise e Desenvolvimento de Sistemas",
        0,
        1,
        listOf(CertificadoDoEgresso(certificadoId, "Seminário", "EVENTO", agora, hash)),
    )

    "painel nao oferece criacao e diploma fica nulo" {
        val useCase = ObterPainelEgressoUseCase(FakeConsulta(cadastro, null))
        val painel = useCase.execute(usuarioId, listOf("alumni.view_own"))
        val response = EgressoAssembler().from(painel)
        response.nome shouldBe "Egresso Dev"
        response.curso shouldBe "Análise e Desenvolvimento de Sistemas"
        response.concluidoEm.shouldBeNull()
        response.diploma.shouldBeNull()
        response.colacao.shouldBeNull()
        response.kpis.situacaoDiploma.shouldBeNull()
        response.kpis.horasFormativasValidadas shouldBe 0
        response.kpis.certificadosEmitidos shouldBe 1
        response.links.keys shouldContainExactly setOf("self")
        response.links["self"] shouldBe "/egressos/me"
        response.certificados.single().links.keys shouldContainExactly setOf("reemitir")
        response.certificados.single().links["reemitir"] shouldBe
            "/egressos/me/certificados/$certificadoId/reemissao"
        response.certificados.single().hashSha256 shouldBe hash
    }

    "aluno ativo nao consulta o painel" {
        val fake = FakeConsulta(cadastro, null)
        val useCase = ObterPainelEgressoUseCase(fake)
        shouldThrow<AcessoNegadoException> {
            useCase.execute(usuarioId, listOf("alumni.view_own", "request.open"))
        }
        fake.consultas shouldBe 0
    }

    "matriculado com a cap nao entra no portal" {
        val matriculado = cadastro.copy(egresso = false)
        val useCase = ObterPainelEgressoUseCase(FakeConsulta(matriculado, null))
        shouldThrow<AcessoNegadoException> {
            useCase.execute(usuarioId, listOf("alumni.view_own"))
        }
    }

    "reemitir devolve o mesmo pdf e 404 se nao for o dono" {
        val arquivo = PdfDoEgresso(certificadoId, "Seminário", pdf, hash)
        val fake = FakeConsulta(cadastro, arquivo)
        val useCase = ReemitirCertificadoEgressoUseCase(fake)
        val baixado = useCase.execute(usuarioId, listOf("alumni.view_own"), certificadoId)
        baixado.pdf.toList() shouldBe pdf.toList()
        baixado.hashSha256 shouldBe hash
        fake.gravacoes shouldBe 0
        shouldThrow<RecursoNaoEncontradoException> {
            useCase.execute(usuarioId, listOf("alumni.view_own"), UUID.randomUUID())
        }
    }

    "reemitir recusa aluno ativo antes de ler o arquivo" {
        val fake = FakeConsulta(cadastro, PdfDoEgresso(certificadoId, "Seminário", pdf, hash))
        val useCase = ReemitirCertificadoEgressoUseCase(fake)
        shouldThrow<AcessoNegadoException> {
            useCase.execute(usuarioId, listOf("alumni.view_own", "tcc.view_own"), certificadoId)
        }
        fake.consultas shouldBe 0
    }
})

private class FakeConsulta(
    private val cadastro: Cadastro?,
    private val pdf: PdfDoEgresso?,
) : EgressoConsultaPort {
    var consultas: Int = 0
    var gravacoes: Int = 0

    override fun consultar(usuarioId: UUID): Cadastro? {
        consultas++
        return cadastro
    }

    override fun certificado(certificadoId: UUID, alunoId: UUID): PdfDoEgresso? {
        val arquivo = pdf ?: return null
        if (cadastro == null || cadastro.alunoId != alunoId || arquivo.id != certificadoId) {
            return null
        }
        return arquivo
    }
}
