package br.ufpr.sept.so2.modules.formativas.application.ports

import java.util.UUID

interface AlunoPorUsuarioPort {
    fun resolver(usuarioId: UUID): AlunoRef?

    data class AlunoRef(
        val id: UUID,
        val egresso: Boolean,
        val horasFormativasMinimas: Int,
    )
}
