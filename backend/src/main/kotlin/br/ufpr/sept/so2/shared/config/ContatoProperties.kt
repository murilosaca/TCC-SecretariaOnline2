package br.ufpr.sept.so2.shared.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.contato")
class ContatoProperties {
    var nome: String? = null
    var endereco: String? = null
    var telefone: String? = null
    var email: String? = null
    var horario: String? = null
}
