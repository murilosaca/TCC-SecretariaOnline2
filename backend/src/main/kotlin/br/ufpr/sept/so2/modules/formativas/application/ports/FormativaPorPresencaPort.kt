package br.ufpr.sept.so2.modules.formativas.application.ports

import java.util.UUID

fun interface FormativaPorPresencaPort {
    fun criarPendenteSeAusente(
        eventoId: UUID,
        usuarioId: UUID,
        titulo: String,
        cargaHoraria: Int,
    )
}
