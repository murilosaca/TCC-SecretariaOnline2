package br.ufpr.sept.so2.modules.reports.application

import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCoordenadorQueryPort
import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCursoEscopoPort
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador
import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenadorRegras
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class ObterRelatorioCoordenadorUseCase(
    private val escopoPort: RelatorioCursoEscopoPort,
    private val queryPort: RelatorioCoordenadorQueryPort,
) {
    @Transactional(readOnly = true)
    fun execute(
        usuarioId: UUID,
        authorities: Collection<String>,
        periodoCodigo: String?,
        cursoFiltro: String?,
    ): RelatorioCoordenador {
        RelatorioCoordenadorAcesso.exigirCap(authorities)
        val coordenados = escopoPort.idsCoordenados(usuarioId)
        val cursoId = resolverCursoId(coordenados, cursoFiltro)
        val curso = escopoPort.resolverCurso(cursoId)
            ?: throw br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException(
                RelatorioCoordenadorAcesso.MSG_OUTRO_CURSO,
            )
        RelatorioCoordenadorAcesso.exigirDono(curso.id, coordenados)

        val periodo = resolverPeriodo(periodoCodigo)
        return queryPort.agregar(curso.id, periodo).copy(
            cursoSigla = curso.sigla,
            cursoNome = curso.nome,
            periodoCodigo = periodo?.codigo,
            periodoRotulo = periodo?.rotulo,
        )
    }

    private fun resolverCursoId(coordenados: Set<UUID>, cursoFiltro: String?): UUID {
        if (cursoFiltro.isNullOrBlank()) {
            return RelatorioCoordenadorAcesso.exigirDono(null, coordenados)
        }
        val encontrado = escopoPort.localizarPorSigla(cursoFiltro.trim(), coordenados)
            ?: run {
                // Sigla existe fora do escopo ou é desconhecida: 403 (anti-enumeração / escopo).
                throw br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException(
                    RelatorioCoordenadorAcesso.MSG_OUTRO_CURSO,
                )
            }
        return RelatorioCoordenadorAcesso.exigirDono(encontrado.id, coordenados)
    }

    private fun resolverPeriodo(periodoCodigo: String?): RelatorioCoordenadorQueryPort.PeriodoFiltro? {
        val parsed = try {
            RelatorioCoordenadorRegras.parsePeriodo(periodoCodigo)
        } catch (ex: IllegalArgumentException) {
            throw DadoInvalidoException(ex.message ?: "Período inválido.")
        }
        val periodos = queryPort.listarPeriodos()
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
}
