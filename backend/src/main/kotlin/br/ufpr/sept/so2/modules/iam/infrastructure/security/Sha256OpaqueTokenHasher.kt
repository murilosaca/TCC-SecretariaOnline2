package br.ufpr.sept.so2.modules.iam.infrastructure.security

import br.ufpr.sept.so2.modules.iam.application.ports.OpaqueTokenHasher
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.HexFormat

@Component
class Sha256OpaqueTokenHasher : OpaqueTokenHasher {
    override fun hash(rawToken: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(rawToken.toByteArray(StandardCharsets.UTF_8))
            HexFormat.of().formatHex(bytes)
        } catch (ex: NoSuchAlgorithmException) {
            throw IllegalStateException("SHA-256 indisponível", ex)
        }
    }
}
