package br.ufpr.sept.so2.modules.bff.application.ports

import java.util.UUID

fun interface CertificadosDashboardQueryPort {
    fun contarDoAluno(usuarioId: UUID): Int?
}
