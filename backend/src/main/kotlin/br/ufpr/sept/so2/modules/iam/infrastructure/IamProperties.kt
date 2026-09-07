package br.ufpr.sept.so2.modules.iam.infrastructure

import br.ufpr.sept.so2.modules.iam.application.IamSettings
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.iam")
class IamProperties : IamSettings {
    override var accessTtlSeconds: Long = 900
    override var refreshTtlSeconds: Long = 604800
    override var resetTtlSeconds: Long = 86400
    var cookieName: String = "so2_refresh"
    var cookieSecure: Boolean = false
    var cookieSameSite: String = "Lax"
    var issuer: String = "so2"
    var audience: String = "so2-api"
    var resetAudience: String = "password-reset"
    override var frontendBaseUrl: String = "http://localhost:5173"
    var jwtPrivateKeyPem: String = ""
    var jwtPublicKeyPem: String = ""
    override var maxFalhasConsecutivas: Int = 10
    override var minutosBloqueio: Int = 15
    var loginPorMinuto: Int = 5
    var recuperarPorHora: Int = 3
    val argon2: Argon2 = Argon2()
    val seed: Seed = Seed()

    class Argon2 {
        var saltLength: Int = 16
        var hashLength: Int = 32
        var parallelism: Int = 1
        var memoryKb: Int = 48128
        var iterations: Int = 1
    }

    class Seed {
        var enabled: Boolean = false
        var password: String = ""
    }
}
