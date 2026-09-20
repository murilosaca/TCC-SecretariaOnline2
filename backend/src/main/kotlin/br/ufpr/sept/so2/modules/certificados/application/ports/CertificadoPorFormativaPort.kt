package br.ufpr.sept.so2.modules.certificados.application.ports

import java.util.UUID

fun interface CertificadoPorFormativaPort {
    fun emitirSeAusente(
        formativaId: UUID,
        alunoId: UUID,
        eventoId: UUID?,
        titulo: String,
        cargaHoraria: Int,
        atorId: UUID,
        ip: String?,
    )
}
