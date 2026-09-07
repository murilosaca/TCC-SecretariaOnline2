package br.ufpr.sept.so2.modules.bff.application.ports

import java.time.LocalDate
import java.util.UUID

fun interface PeriodoVigenteQueryPort {
    fun consultar(data: LocalDate): PeriodoVigenteResumo?

    data class PeriodoVigenteResumo(
        val id: UUID,
        val ano: Int,
        val semestre: Int,
        val inicio: LocalDate,
        val fim: LocalDate,
    ) {
        fun rotulo(): String = "$ano/$semestre"
    }
}
