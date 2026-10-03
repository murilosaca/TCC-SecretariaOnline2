package br.ufpr.sept.so2.modules.importacao.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class ExecutarLotesTest {
    @Test
    fun falhaDoLoteSeguinteMantemOAnteriorComoParcial() {
        val lotes = listOf(
            listOf(linha(1)),
            listOf(linha(2)),
        )
        var chamadas = 0
        val resultado = executarLotes(lotes) {
            chamadas += 1
            if (chamadas == 2) {
                throw IllegalStateException("lote seguinte")
            }
        }
        assertEquals(ImportacaoStatus.PARTIAL, resultado.status)
        assertEquals(1, resultado.importadas)
        assertEquals(1, resultado.naoProcessadas)
    }

    private fun linha(numero: Int) = ImportacaoLinha(UUID.randomUUID(), numero, ImportacaoStatus.VALID, null, emptyMap())
}
