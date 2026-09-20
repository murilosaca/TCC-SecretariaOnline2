package br.ufpr.sept.so2.modules.certificados.infrastructure

import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoSigner
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Component
class Ed25519CertificadoSigner(
    @Value("\${app.certificados.ed25519-private-key-pem:}") privateKeyPem: String,
    @Value("\${app.certificados.ed25519-public-key-pem:}") publicKeyPem: String,
) : CertificadoSigner {

    private val privateKey: PrivateKey
    private val publicKey: PublicKey

    init {
        val pair = resolveKeyPair(privateKeyPem, publicKeyPem)
        privateKey = pair.private
        publicKey = pair.public
    }

    override fun sign(hashSha256: ByteArray): String {
        val signature = Signature.getInstance("Ed25519")
        signature.initSign(privateKey)
        signature.update(hashSha256)
        return Base64.getEncoder().encodeToString(signature.sign())
    }

    override fun publicJwk(): Map<String, Any> {
        val encoded = publicKey.encoded
        val raw = encoded.copyOfRange(encoded.size - 32, encoded.size)
        return linkedMapOf(
            "kty" to "OKP",
            "crv" to "Ed25519",
            "x" to Base64.getUrlEncoder().withoutPadding().encodeToString(raw),
            "kid" to KID,
            "use" to "sig",
            "alg" to "EdDSA",
        )
    }

    companion object {
        const val KID = "so2-cert-ed25519"
        private val LOG = LoggerFactory.getLogger(Ed25519CertificadoSigner::class.java)

        private fun resolveKeyPair(privatePem: String, publicPem: String): KeyPair {
            if (privatePem.isNotBlank() && publicPem.isNotBlank()) {
                return parse(privatePem, publicPem)
            }
            if (privatePem.isNotBlank()) {
                return fromPrivate(privatePem)
            }
            LOG.warn(
                "CERT_ED25519_* ausente; usando par de desenvolvimento derivado da chave RFC 8410. " +
                    "Produção deve definir CERT_ED25519_PRIVATE_KEY (e a pública, ou deixar derivar).",
            )
            return fromPrivate(DEV_PRIVATE_PEM)
        }

        private fun parse(privatePem: String, publicPem: String): KeyPair {
            val factory = KeyFactory.getInstance("Ed25519")
            val privateKey = factory.generatePrivate(PKCS8EncodedKeySpec(decodePem(privatePem, "PRIVATE KEY")))
            val publicKey = factory.generatePublic(X509EncodedKeySpec(decodePem(publicPem, "PUBLIC KEY")))
            return KeyPair(publicKey, privateKey)
        }

        private fun fromPrivate(privatePem: String): KeyPair {
            val der = decodePem(privatePem, "PRIVATE KEY")
            val factory = KeyFactory.getInstance("Ed25519")
            val privateKey = factory.generatePrivate(PKCS8EncodedKeySpec(der))
            val privParams = PrivateKeyFactory.createKey(der) as Ed25519PrivateKeyParameters
            val pubInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(privParams.generatePublicKey())
            val publicKey = factory.generatePublic(X509EncodedKeySpec(pubInfo.encoded))
            return KeyPair(publicKey, privateKey)
        }

        private fun decodePem(pem: String, type: String): ByteArray {
            val cleaned = pem
                .replace("-----BEGIN $type-----", "")
                .replace("-----END $type-----", "")
                .replace(Regex("\\s"), "")
            return Base64.getDecoder().decode(cleaned)
        }

        // PKCS#8 de exemplo RFC 8410 (dev/test). A pública é derivada. Não usar em produção.
        private const val DEV_PRIVATE_PEM =
            "-----BEGIN PRIVATE KEY-----\n" +
                "MC4CAQAwBQYDK2VwBCIEINTuctv5E1hK1bbY8fdp+K06/nwoy/HU++C1dIwa9BlS\n" +
                "-----END PRIVATE KEY-----"
    }
}
