package br.ufpr.sept.so2.modules.certificados.api

import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoSigner
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@Tag(name = "JWKS", description = "Chave pública ED25519 dos certificados (RF-F0-007 / RNF-LGL-02)")
class JwksController(
    private val certificadoSigner: CertificadoSigner,
) {

    @GetMapping("/.well-known/jwks.json")
    @Operation(summary = "JWKS com a chave pública ED25519 do servidor")
    fun jwks(): ResponseEntity<Map<String, Any>> {
        val body = mapOf("keys" to listOf(certificadoSigner.publicJwk()))
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
            .body(body)
    }
}
