package br.ufpr.sept.so2.modules.formativas.application.ports

import java.util.UUID

fun interface AlunoResumoPort {
    fun nomeDe(alunoId: UUID): String?
}
