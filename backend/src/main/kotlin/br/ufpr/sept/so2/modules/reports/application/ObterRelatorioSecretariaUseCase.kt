package br.ufpr.sept.so2.modules.reports.application

import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCoordenadorQueryPort
import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCursoEscopoPort
import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioSecretariaQueryPort
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenadorRegras
import br.ufpr.sept.so2.modules.reports.domain.RelatorioSecretaria
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class ObterRelatorioSecretariaUseCase(
    private val escopoPort: RelatorioCursoEscopoPort,
    private val queryPort: RelatorioSecretariaQueryPort,
    private val periodoPort: RelatorioCoordenadorQueryPort,
) {
    @Transactional(readOnly = true)
    fun execute(
        usuarioId: UUID,
        authorities: Collection<String>,
        periodoCodigo: String?,
        cursoFiltro: String?,
    ): RelatorioSecretaria {
        RelatorioSecretariaAcesso.exigirCap(authorities)
        val vinculados = escopoPort.idsSecretariados(usuarioId)
        RelatorioSecretariaAcesso.exigirEscopo(vinculados)

        val cursosAlvo = resolverCursos(vinculados, cursoFiltro)
        val periodo = resolverPeriodo(periodoCodigo)
        val agregado = queryPort.agregar(cursosAlvo.map { it.id }.toSet(), periodo)
        val filtradoPorCurso = !cursoFiltro.isNullOrBlank()
        val cursoUnico = if (filtradoPorCurso) cursosAlvo.singleOrNull() else null

        return agregado.copy(
            cursoId = cursoUnico?.id,
            cursoSigla = cursoUnico?.sigla,
            cursoNome = cursoUnico?.nome ?: ROTULO_TODOS,
            cursosEscopo = cursosAlvo.map {
                RelatorioSecretaria.CursoEscopoResumo(it.id, it.sigla, it.nome)
            },
            periodoCodigo = periodo?.codigo,
            periodoRotulo = periodo?.rotulo,
        )
    }

    private fun resolverCursos(
        vinculados: Set<UUID>,
        cursoFiltro: String?,
    ): List<RelatorioCursoEscopoPort.CursoEscopo> {
        if (cursoFiltro.isNullOrBlank()) {
            return escopoPort.listarCursos(vinculados)
        }
        val encontrado = escopoPort.localizarPorSigla(cursoFiltro.trim(), vinculados)
            ?: throw AcessoNegadoException(RelatorioSecretariaAcesso.MSG_FORA_ESCOPO)
        RelatorioSecretariaAcesso.exigirCursoNoEscopo(encontrado.id, vinculados)
        return listOf(encontrado)
    }

    private fun resolverPeriodo(periodoCodigo: String?): RelatorioCoordenadorQueryPort.PeriodoFiltro? {
        val parsed = try {
            RelatorioCoordenadorRegras.parsePeriodo(periodoCodigo)
        } catch (ex: IllegalArgumentException) {
            throw DadoInvalidoException(ex.message ?: "Período inválido.")
        }
        val periodos = periodoPort.listarPeriodos()
        if (parsed == null) {
            val hoje = LocalDate.now()
            return periodos.firstOrNull { !hoje.isBefore(it.inicio) && !hoje.isAfter(it.fim) }?.paraFiltro()
                ?: periodos.maxWithOrNull(compareBy({ it.ano }, { it.semestre }))?.paraFiltro()
        }
        val match = periodos.firstOrNull { it.bate(parsed) }
            ?: throw DadoInvalidoException(
                "Período letivo ${RelatorioCoordenadorRegras.rotulo(parsed.ano, parsed.semestre)} não encontrado.",
            )
        return match.paraFiltro()
    }

    companion object {
        const val ROTULO_TODOS: String = "Cursos vinculados"
    }
}
