package br.ufpr.sept.so2.modules.certificados.application.ports

interface CertificadoSigner {
    fun sign(hashSha256: ByteArray): String

    fun publicJwk(): Map<String, Any>
}
