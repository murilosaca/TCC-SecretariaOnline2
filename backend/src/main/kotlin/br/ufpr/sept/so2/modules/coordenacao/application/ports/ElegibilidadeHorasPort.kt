package br.ufpr.sept.so2.modules.coordenacao.application.ports

import java.util.UUID

interface ElegibilidadeHorasPort {
    fun congelarQuemJaAtingiu(idCurso: UUID, limiarAnterior: Int)

    fun requeridas(alunoId: UUID, limiarAtual: Int, horasValidadas: Int): Int
}
