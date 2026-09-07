package br.ufpr.sept.so2.modules.presenca.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.eventos")
class EventoProperties {
    var devPin: String = "123456"
}
