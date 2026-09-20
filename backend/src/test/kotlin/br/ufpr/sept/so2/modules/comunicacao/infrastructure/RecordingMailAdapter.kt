package br.ufpr.sept.so2.modules.comunicacao.infrastructure

import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailMessage
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailPort
import java.util.concurrent.CopyOnWriteArrayList

class RecordingMailAdapter : MailPort {
    val mensagens: MutableList<MailMessage> = CopyOnWriteArrayList()
    @Volatile
    var falhar: Boolean = false

    override fun send(message: MailMessage) {
        if (falhar) {
            throw IllegalStateException("smtp indisponível")
        }
        mensagens.add(message)
    }

    fun limpar() {
        mensagens.clear()
        falhar = false
    }
}
