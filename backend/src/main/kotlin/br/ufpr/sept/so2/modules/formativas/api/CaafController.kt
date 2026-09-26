package br.ufpr.sept.so2.modules.formativas.api

import br.ufpr.sept.so2.modules.formativas.api.dto.AtribuirFormativaRequest
import br.ufpr.sept.so2.modules.formativas.api.dto.BatchAprovarFormativasRequest
import br.ufpr.sept.so2.modules.formativas.api.dto.CaafPoolResponse
import br.ufpr.sept.so2.modules.formativas.application.AtribuirFormativaCaafUseCase
import br.ufpr.sept.so2.modules.formativas.application.BatchAprovarFormativasCaafUseCase
import br.ufpr.sept.so2.modules.formativas.application.ObterPoolCaafUseCase
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/comissoes/caaf")
@Tag(name = "CAAF", description = "Pool da comissão de formativas — atribuição e lote (RF-F4-001)")
class CaafController(
    private val obterPoolCaafUseCase: ObterPoolCaafUseCase,
    private val atribuirFormativaCaafUseCase: AtribuirFormativaCaafUseCase,
    private val batchAprovarFormativasCaafUseCase: BatchAprovarFormativasCaafUseCase,
    private val assembler: CaafAssembler,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('formative.review')")
    @Operation(summary = "KPIs e lista do pool CAAF (não atribuídas + comigo)")
    fun pool(authentication: Authentication): CaafPoolResponse {
        val principal = principal(authentication)
        return assembler.from(obterPoolCaafUseCase.execute(principal.userId))
    }

    @PostMapping("/atribuicoes")
    @PreAuthorize("hasAuthority('formative.review')")
    @Operation(summary = "Atribuir uma formativa a um membro da CAAF")
    fun atribuir(
        @RequestBody request: AtribuirFormativaRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): CaafPoolResponse {
        val principal = principal(authentication)
        val formativaId = request.formativaId
            ?: throw DadoInvalidoException("formativaId é obrigatório.")
        val assigneeId = request.assigneeId
            ?: throw DadoInvalidoException("assigneeId é obrigatório.")
        return assembler.from(
            atribuirFormativaCaafUseCase.execute(
                principal.userId,
                formativaId,
                assigneeId,
                clientIp(http),
            ),
        )
    }

    @PostMapping("/lote")
    @PreAuthorize("hasAuthority('formative.review')")
    @Operation(summary = "Aprovar em lote formativas com presença já validada")
    fun lote(
        @RequestBody request: BatchAprovarFormativasRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): CaafPoolResponse {
        val principal = principal(authentication)
        return assembler.from(
            batchAprovarFormativasCaafUseCase.execute(
                principal.userId,
                request.ids,
                request.decisao,
                clientIp(http),
            ),
        )
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal

        private fun clientIp(request: HttpServletRequest): String {
            val forwarded = request.getHeader("X-Forwarded-For")
            if (!forwarded.isNullOrBlank()) {
                return forwarded.split(",")[0].trim()
            }
            return request.remoteAddr
        }
    }
}
