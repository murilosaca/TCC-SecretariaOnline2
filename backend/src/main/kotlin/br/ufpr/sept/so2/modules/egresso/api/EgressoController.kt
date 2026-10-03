package br.ufpr.sept.so2.modules.egresso.api

import br.ufpr.sept.so2.modules.arquivos.api.dto.DownloadUrlResponse
import br.ufpr.sept.so2.modules.arquivos.application.PresignDownloadUseCase
import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoItemResponse
import br.ufpr.sept.so2.modules.egresso.api.dto.EgressoPainelResponse
import br.ufpr.sept.so2.modules.egresso.application.BaixarDiplomaEgressoUseCase
import br.ufpr.sept.so2.modules.egresso.application.ListarEgressosUseCase
import br.ufpr.sept.so2.modules.egresso.application.ObterPainelEgressoUseCase
import br.ufpr.sept.so2.modules.egresso.application.ReemitirCertificadoEgressoUseCase
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
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
import java.time.LocalDate
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/egressos")
@Tag(name = "Egressos", description = "Portal read-only do egresso (RF-F2-001)")
class EgressoController(
    private val obterPainelEgressoUseCase: ObterPainelEgressoUseCase,
    private val reemitirCertificadoEgressoUseCase: ReemitirCertificadoEgressoUseCase,
    private val baixarDiplomaEgressoUseCase: BaixarDiplomaEgressoUseCase,
    private val listarEgressosUseCase: ListarEgressosUseCase,
    private val presignDownloadUseCase: PresignDownloadUseCase,
    private val assembler: EgressoAssembler,
) {

    /**
     * F5.10: lista read-only da secretaria. `?format=csv` devolve `text/csv`
     * síncrono do mesmo filtro — não existe job de exportação nesta fatia.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('alumni.list')")
    @Operation(summary = "Listar egressos dos cursos da secretaria (filtros curso, ano e situação)")
    fun listar(
        @RequestParam(required = false) cursoId: UUID?,
        @RequestParam(required = false) ano: Int?,
        @RequestParam(required = false) situacao: String?,
        @RequestParam(required = false) format: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): Any {
        val atorId = principal(authentication).userId
        val pagina = listarEgressosUseCase.execute(atorId, cursoId, ano, situacao, pageable)
        if ("csv".equals(format, ignoreCase = true)) {
            val nome = "egressos-${LocalDate.now()}.csv"
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$nome\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(EgressoCsvMarshaller.egressos(pagina.content.map(EgressoItemResponse::from)))
        }
        return PageResponse.ofWithLinks(pagina, Function { EgressoItemResponse.from(it) })
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('alumni.view_own')")
    @Operation(summary = "Painel read-only do egresso autenticado")
    fun me(authentication: Authentication): EgressoPainelResponse {
        val principal = principal(authentication)
        return assembler.from(obterPainelEgressoUseCase.execute(principal.userId, principal.authorities))
    }

    @GetMapping("/me/diploma")
    @PreAuthorize("hasAuthority('alumni.view_own')")
    @Operation(summary = "URL pré-assinada do PDF oficial do diploma (TTL 15 min)")
    fun baixarDiploma(authentication: Authentication): DownloadUrlResponse {
        val principal = principal(authentication)
        val diploma = baixarDiplomaEgressoUseCase.execute(principal.userId, principal.authorities)
        val url = presignDownloadUseCase.execute(
            diploma.storageKey!!,
            "diploma-${diploma.numero}.pdf",
        )
        return DownloadUrlResponse(url, presignDownloadUseCase.ttlSeconds())
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
