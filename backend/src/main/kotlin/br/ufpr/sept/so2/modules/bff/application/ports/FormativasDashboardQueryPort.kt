package br.ufpr.sept.so2.modules.bff.application.ports

import java.util.UUID

fun interface FormativasDashboardQueryPort {
    fun consultar(usuarioId: UUID): FormativasDashboard

    data class FormativasDashboard(
        val horasValidadas: Int,
        val horasRequeridas: Int,
        val pendentes: List<PendenciaFormativa>,
    )

    data class PendenciaFormativa(
        val id: UUID,
        val titulo: String,
        val estado: String,
        val href: String,
    )
}
