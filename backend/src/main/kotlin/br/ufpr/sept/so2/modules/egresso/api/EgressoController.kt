package br.ufpr.sept.so2.modules.egresso.api

import br.ufpr.sept.so2.modules.arquivos.api.dto.DownloadUrlResponse
import br.ufpr.sept.so2.modules.arquivos.application.PresignDownloadUseCase
import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoPainelResponse
import br.ufpr.sept.so2.modules.egresso.application.ObterPainelEgressoUseCase
import br.ufpr.sept.so2.modules.egresso.application.ReemitirCertificadoEgressoUseCase
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/egressos")
@Tag(name = "Egressos", description = "Portal read-only do egresso (RF-F2-001)")
class EgressoController(
    private val obterPainelEgressoUseCase: ObterPainelEgressoUseCase,
    private val reemitirCertificadoEgressoUseCase: ReemitirCertificadoEgressoUseCase,
    private val presignDownloadUseCase: PresignDownloadUseCase,
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
    @Operation(summary = "URL pré-assinada do PDF já gravado, sem nova assinatura (TTL 15 min)")
    fun reemitir(@PathVariable id: UUID, authentication: Authentication): DownloadUrlResponse {
        val principal = principal(authentication)
        val arquivo = reemitirCertificadoEgressoUseCase.execute(
            principal.userId,
            principal.authorities,
            id,
        )
        val url = presignDownloadUseCase.execute(
            arquivo.storageKey,
            "certificado-${arquivo.id}.pdf",
        )
        return DownloadUrlResponse(url, presignDownloadUseCase.ttlSeconds())
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}
