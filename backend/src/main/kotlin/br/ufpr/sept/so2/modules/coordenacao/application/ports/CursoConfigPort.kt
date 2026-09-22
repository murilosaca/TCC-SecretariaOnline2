package br.ufpr.sept.so2.modules.coordenacao.application.ports

import java.util.UUID

interface CursoConfigPort {
    fun obterCurso(id: UUID, bloquear: Boolean): CursoResumo?

    fun obterParametros(id: UUID): ParametrosBanca?

    fun salvarHoras(id: UUID, horas: Int)

    fun salvarParametros(parametros: ParametrosBanca)

    data class CursoResumo(
        val id: UUID,
        val nome: String,
        val sigla: String,
        val idCoordenador: UUID?,
        val horasFormativasMinimas: Int,
        val ativo: Boolean,
    )

    data class ParametrosBanca(
        val idCurso: UUID,
        val duracaoCalendario: Int,
        val bancaMembrosExternos: Int,
        val bancaModalidade: String,
        val regimento: String,
    )
}
