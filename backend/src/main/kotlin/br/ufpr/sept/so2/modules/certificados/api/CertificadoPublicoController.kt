package br.ufpr.sept.so2.modules.certificados.api

import br.ufpr.sept.so2.modules.certificados.api.dto.VerificacaoCertificadoResponse
import br.ufpr.sept.so2.modules.certificados.application.VerificarCertificadoUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/publico/certificados")
@Tag(name = "Certificados públicos", description = "Verificação anônima F0.7 (RF-F0-007)")
class CertificadoPublicoController(
    private val verificarCertificadoUseCase: VerificarCertificadoUseCase,
) {

    @GetMapping("/{hash}/verificacao")
    @Operation(summary = "Metadados e assinatura ED25519 para verificação no browser")
    fun verificar(@PathVariable hash: String): VerificacaoCertificadoResponse {
        val certificado = verificarCertificadoUseCase.execute(hash)
        return VerificacaoCertificadoResponse(
            STATUS_VALIDO,
            certificado.hashSha256,
            certificado.assinatura,
            certificado.beneficiarioNome,
            certificado.titulo,
            certificado.cargaHoraria,
            certificado.emitidoEm,
            JWKS_URL,
        )
    }

    companion object {
        const val STATUS_VALIDO = "VALIDO"
        const val JWKS_URL = "/.well-known/jwks.json"
    }
}
