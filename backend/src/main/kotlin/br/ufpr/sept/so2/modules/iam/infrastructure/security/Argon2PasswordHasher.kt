package br.ufpr.sept.so2.modules.iam.infrastructure.security

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Component

@Component
class Argon2PasswordHasher(
    properties: IamProperties,
) : PasswordHasher {
    private val encoder: Argon2PasswordEncoder
    private val dummyHash: String

    init {
        val argon2 = properties.argon2
        encoder = Argon2PasswordEncoder(
            argon2.saltLength,
            argon2.hashLength,
            argon2.parallelism,
            argon2.memoryKb,
            argon2.iterations,
        )
        dummyHash = encoder.encode("so2-dummy-timing")
    }

    override fun hash(raw: String): String = encoder.encode(raw)

    override fun matches(raw: String?, hash: String?): Boolean {
        if (raw == null || hash == null) {
            return false
        }
        return encoder.matches(raw, hash)
    }

    override fun matchesDummy() {
        encoder.matches("so2-dummy-timing", dummyHash)
    }
}
