package br.ufpr.sept.so2.modules.academico.application.ports

import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

interface PeriodoLetivoRepository {
    fun save(periodo: PeriodoLetivo): PeriodoLetivo

    fun findById(id: UUID): Optional<PeriodoLetivo>

    fun findAll(pageable: Pageable): Page<PeriodoLetivo>

    fun findAll(): List<PeriodoLetivo>

    fun findVigente(data: LocalDate): Optional<PeriodoLetivo>

    fun existsByAnoAndSemestre(ano: Int, semestre: Int): Boolean

    fun deleteById(id: UUID)
}
