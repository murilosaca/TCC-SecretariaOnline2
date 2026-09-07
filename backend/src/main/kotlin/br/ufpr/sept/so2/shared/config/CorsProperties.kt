package br.ufpr.sept.so2.shared.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.cors")
class CorsProperties {
    var allowedOrigins: MutableList<String> = mutableListOf()
}
