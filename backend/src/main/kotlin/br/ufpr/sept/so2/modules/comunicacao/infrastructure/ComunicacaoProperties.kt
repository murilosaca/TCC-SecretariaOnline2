package br.ufpr.sept.so2.modules.comunicacao.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.comunicacao")
class ComunicacaoProperties {
    var from: String = "so2@localhost"
    var dispatcherEnabled: Boolean = true
    var lote: Int = 20
    var maxTentativas: Int = 5
    var staleProcessingSeconds: Long = 60
}
