package br.ufpr.sept.so2.modules.comunicacao.application.ports

data class MailMessage(
    val to: String,
    val subject: String,
    val body: String,
)

interface MailPort {
    fun send(message: MailMessage)
}
