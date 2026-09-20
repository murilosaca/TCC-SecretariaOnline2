package br.ufpr.sept.so2.modules.certificados.api

import br.ufpr.sept.so2.modules.certificados.api.dto.CertificadoResponse
import br.ufpr.sept.so2.modules.certificados.application.ListarMeusCertificadosUseCase
import br.ufpr.sept.so2.modules.certificados.application.ObterCertificadoUseCase
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/certificates")
@Tag(name = "Certificados", description = "Certificados oficiais emitidos pelo sistema (RF-F1-010 / RF-TR-003)")
class CertificadoController(
    private val listarMeusCertificadosUseCase: ListarMeusCertificadosUseCase,
    private val obterCertificadoUseCase: ObterCertificadoUseCase,
    private val assembler: CertificadoAssembler,
) {

    @GetMapping
    @PreAuthorize("hasAuthority('certificate.view_own')")
    @Operation(summary = "Listar certificados do aluno autenticado")
    fun listar(
        @RequestParam(defaultValue = "me") beneficiario: String,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<CertificadoResponse> {
        val principal = principal(authentication)
        return PageResponse.ofWithLinks(
            listarMeusCertificadosUseCase.execute(principal.userId, beneficiario, pageable),
            Function { assembler.from(it) },
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('certificate.view_own')")
    @Operation(summary = "Detalhe do certificado do dono")
    fun buscar(@PathVariable id: UUID, authentication: Authentication): CertificadoResponse {
        val principal = principal(authentication)
        return assembler.from(obterCertificadoUseCase.execute(id, principal.userId))
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('certificate.view_own')")
    @Operation(summary = "Download do PDF canônico do dono")
    fun baixar(@PathVariable id: UUID, authentication: Authentication): ResponseEntity<ByteArray> {
        val principal = principal(authentication)
        val certificado = obterCertificadoUseCase.execute(id, principal.userId)
        val disposition = ContentDisposition.attachment()
            .filename("certificado-${certificado.id}.pdf", StandardCharsets.UTF_8)
            .build()
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .body(certificado.pdf)
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}
