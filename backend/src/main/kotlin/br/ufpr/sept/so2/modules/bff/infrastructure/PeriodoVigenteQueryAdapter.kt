package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository
import br.ufpr.sept.so2.modules.bff.application.ports.PeriodoVigenteQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.PeriodoVigenteQueryPort.PeriodoVigenteResumo
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class PeriodoVigenteQueryAdapter(
    private val periodoLetivoRepository: PeriodoLetivoRepository,
) : PeriodoVigenteQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(data: LocalDate): PeriodoVigenteResumo? =
        periodoLetivoRepository.findVigente(data)
            .map { periodo ->
                PeriodoVigenteResumo(
                    periodo.id,
                    periodo.ano,
                    periodo.semestre,
                    periodo.inicio,
                    periodo.fim,
                )
            }
            .orElse(null)
}
