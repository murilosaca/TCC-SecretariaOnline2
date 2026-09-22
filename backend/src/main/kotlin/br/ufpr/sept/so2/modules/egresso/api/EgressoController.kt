package br.ufpr.sept.so2.modules.egresso.api

import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoPainelResponse
import br.ufpr.sept.so2.modules.egresso.application.ObterPainelEgressoUseCase
import br.ufpr.sept.so2.modules.egresso.application.ReemitirCertificadoEgressoUseCase
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets
import java.util.UUID

@RestController
@RequestMapping("/egressos")
@Tag(name = "Egressos", description = "Portal read-only do egresso (RF-F2-001)")
class EgressoController(
    private val obterPainelEgressoUseCase: ObterPainelEgressoUseCase,
    private val reemitirCertificadoEgressoUseCase: ReemitirCertificadoEgressoUseCase,
    private val assembler: EgressoAssembler,
) {

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('alumni.view_own')")
    @Operation(summary = "Painel read-only do egresso autenticado")
    fun me(authentication: Authentication): EgressoPainelResponse {
        val principal = principal(authentication)
        return assembler.from(obterPainelEgressoUseCase.execute(principal.userId, principal.authorities))
    }

    @GetMapping("/me/certificados/{id}/reemissao")
    @PreAuthorize("hasAuthority('alumni.view_own')")
    @Operation(summary = "Reemitir o PDF já gravado, sem nova assinatura")
    fun reemitir(@PathVariable id: UUID, authentication: Authentication): ResponseEntity<ByteArray> {
        val principal = principal(authentication)
        val arquivo = reemitirCertificadoEgressoUseCase.execute(
            principal.userId,
            principal.authorities,
            id,
        )
        val disposition = ContentDisposition.attachment()
            .filename("certificado-${arquivo.id}.pdf", StandardCharsets.UTF_8)
            .build()
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .body(arquivo.pdf)
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}
