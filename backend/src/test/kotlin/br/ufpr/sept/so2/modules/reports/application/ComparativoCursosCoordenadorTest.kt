package br.ufpr.sept.so2.modules.reports.application

import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCoordenadorQueryPort
import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCursoEscopoPort
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.UUID

class ComparativoCursosCoordenadorTest : StringSpec({
    val usuario = UUID.fromString("01800000-0000-7000-8000-0000000000a1")
    val tads = UUID.fromString("01800000-0000-7000-8000-0000000000c1")
    val si = UUID.fromString("01800000-0000-7000-8000-0000000000c2")

    "comparativo traz só os cursos que o coordenador coordena" {
        val escopo = mockk<RelatorioCursoEscopoPort>()
        val query = mockk<RelatorioCoordenadorQueryPort>()
        every { escopo.idsCoordenados(usuario) } returns setOf(tads, si)
        every { escopo.localizarPorSigla("TADS", any()) } returns curso(tads, "TADS")
        every { escopo.resolverCurso(tads) } returns curso(tads, "TADS")
        every { escopo.resolverCurso(si) } returns curso(si, "SI")
        every { query.listarPeriodos() } returns emptyList()
        every { query.agregar(tads, null) } returns relatorio(tads, "TADS", 40)
        every { query.agregar(si, null) } returns relatorio(si, "SI", 12)
        val useCase = ObterRelatorioCoordenadorUseCase(escopo, query)

        val relatorio = useCase.execute(usuario, listOf("report.view_coordinator"), null, "TADS")

        relatorio.comparativo.map { it.cursoSigla to it.horasValidadas } shouldContainExactly listOf("SI" to 12, "TADS" to 40)
    }

    "outro curso continua 403" {
        val escopo = mockk<RelatorioCursoEscopoPort>()
        val query = mockk<RelatorioCoordenadorQueryPort>()
        every { escopo.idsCoordenados(usuario) } returns setOf(tads)
        every { escopo.localizarPorSigla("DIREITO", any()) } returns null
        val useCase = ObterRelatorioCoordenadorUseCase(escopo, query)

        val erro = shouldThrow<AcessoNegadoException> {
            useCase.execute(usuario, listOf("report.view_coordinator"), null, "DIREITO")
        }
        erro.message shouldBe RelatorioCoordenadorAcesso.MSG_OUTRO_CURSO
    }
})

private fun curso(id: UUID, sigla: String) =
    RelatorioCursoEscopoPort.CursoEscopo(id, sigla, sigla, null)

private fun relatorio(id: UUID, sigla: String, horas: Int) =
    RelatorioCoordenador(
        cursoId = id,
        cursoSigla = sigla,
        cursoNome = sigla,
        periodoCodigo = null,
        periodoRotulo = null,
        kpis = RelatorioCoordenador.KpisCoordenador(1.0, 0.1, horas, 0.5, 0.2),
        series = RelatorioCoordenador.SeriesCoordenador(emptyList(), emptyList(), emptyList()),
        pendencias = emptyList(),
        cargaPorDeliberador = emptyList(),
    )
