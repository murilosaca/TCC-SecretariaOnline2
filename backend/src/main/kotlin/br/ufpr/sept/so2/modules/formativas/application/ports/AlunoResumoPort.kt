package br.ufpr.sept.so2.modules.formativas.application.ports

import java.util.UUID

interface AlunoResumoPort {
    fun nomeDe(alunoId: UUID): String?

    fun cursoDe(alunoId: UUID): UUID?
}
