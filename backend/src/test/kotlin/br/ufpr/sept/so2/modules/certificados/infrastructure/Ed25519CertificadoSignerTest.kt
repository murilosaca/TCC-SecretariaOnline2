package br.ufpr.sept.so2.modules.certificados.infrastructure

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.util.Base64

class Ed25519CertificadoSignerTest : StringSpec({
    "par de desenvolvimento assina e a JWK pública confere" {
        val signer = Ed25519CertificadoSigner("", "")
        val hash = ByteArray(32) { 9 }
        val assinatura = Base64.getDecoder().decode(signer.sign(hash))
        val x = signer.publicJwk()["x"] as String
        val raw = Base64.getUrlDecoder().decode(x)
        val verifier = Ed25519Signer()
        verifier.init(false, Ed25519PublicKeyParameters(raw, 0))
        verifier.update(hash, 0, hash.size)
        verifier.verifySignature(assinatura) shouldBe true
    }
})
