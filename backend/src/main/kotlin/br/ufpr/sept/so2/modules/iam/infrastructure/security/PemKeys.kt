package br.ufpr.sept.so2.modules.iam.infrastructure.security

import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

internal object PemKeys {
    fun generateRsa2048(): KeyPair {
        return try {
            val generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(2048)
            generator.generateKeyPair()
        } catch (ex: Exception) {
            throw IllegalStateException("Não foi possível gerar o par RSA", ex)
        }
    }

    fun parsePrivate(pem: String): PrivateKey {
        return try {
            val der = decode(pem, "PRIVATE KEY")
            KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(der))
        } catch (ex: Exception) {
            throw IllegalStateException("JWT_RSA_PRIVATE_KEY inválida (esperado PKCS#8 PEM)", ex)
        }
    }

    fun parsePublic(pem: String): PublicKey {
        return try {
            val der = decode(pem, "PUBLIC KEY")
            KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(der))
        } catch (ex: Exception) {
            throw IllegalStateException("JWT_RSA_PUBLIC_KEY inválida (esperado X.509 PEM)", ex)
        }
    }

    private fun decode(pem: String, type: String): ByteArray {
        val cleaned = pem
            .replace("-----BEGIN $type-----", "")
            .replace("-----END $type-----", "")
            .replace(Regex("\\s"), "")
        return Base64.getDecoder().decode(cleaned)
    }
}
