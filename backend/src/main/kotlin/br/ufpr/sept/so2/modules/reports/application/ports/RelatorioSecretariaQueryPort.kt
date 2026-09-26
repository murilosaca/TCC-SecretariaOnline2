package br.ufpr.sept.so2.modules.reports.application.ports

import br.ufpr.sept.so2.modules.reports.domain.RelatorioSecretaria
import java.util.UUID

interface RelatorioSecretariaQueryPort {
    fun agregar(
        cursoIds: Set<UUID>,
        periodo: RelatorioCoordenadorQueryPort.PeriodoFiltro?,
    ): RelatorioSecretaria
}
