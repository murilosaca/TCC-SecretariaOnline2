package br.ufpr.sept.so2.modules.comunicacao.application

import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class OutboxDispatcherMonitor {
    private val lock = Any()
    private var ultimoRun: OffsetDateTime? = null
    private var ultimoErro: String? = null

    fun registrarOk() {
        synchronized(lock) {
            ultimoRun = OffsetDateTime.now()
            ultimoErro = null
        }
    }

    fun registrarFalha(mensagem: String?) {
        synchronized(lock) {
            ultimoRun = OffsetDateTime.now()
            ultimoErro = mensagem ?: "falha"
        }
    }

    fun card(): JobCard = synchronized(lock) {
        val agora = OffsetDateTime.now()
        val ultimo = ultimoRun
        val proximo = if (ultimo == null) agora.plusSeconds(INTERVALO_SEGUNDOS) else ultimo.plusSeconds(INTERVALO_SEGUNDOS)
        val status = when {
            ultimoErro != null -> FALHOU
            ultimo != null && agora.isAfter(proximo.plusSeconds(INTERVALO_SEGUNDOS)) -> ATRASADO
            else -> OK
        }
        JobCard("OutboxDispatcher", "5s", ultimo, proximo, status)
    }

    data class JobCard(
        val nome: String,
        val frequencia: String,
        val ultimoRun: OffsetDateTime?,
        val proximoRun: OffsetDateTime?,
        val status: String,
    )

    companion object {
        const val INTERVALO_SEGUNDOS: Long = 5
        const val OK = "OK"
        const val ATRASADO = "ATRASADO"
        const val FALHOU = "FALHOU"
    }
}
