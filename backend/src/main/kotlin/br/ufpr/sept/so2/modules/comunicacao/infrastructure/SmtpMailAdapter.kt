package br.ufpr.sept.so2.modules.comunicacao.infrastructure

import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailMessage
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailPort
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class SmtpMailAdapter(
    private val mailSender: JavaMailSender,
    private val properties: ComunicacaoProperties,
) : MailPort {
    override fun send(message: MailMessage) {
        val smtp = SimpleMailMessage()
        smtp.setFrom(properties.from)
        smtp.setTo(message.to)
        smtp.subject = message.subject
        smtp.text = message.body
        mailSender.send(smtp)
    }
}
