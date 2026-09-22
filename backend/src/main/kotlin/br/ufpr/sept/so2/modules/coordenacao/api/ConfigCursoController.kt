package br.ufpr.sept.so2.modules.coordenacao.api

import br.ufpr.sept.so2.modules.coordenacao.api.dto.ConfigCursoRequest
import br.ufpr.sept.so2.modules.coordenacao.api.dto.ConfigCursoResponse
import br.ufpr.sept.so2.modules.coordenacao.application.AtualizarConfigCursoUseCase
import br.ufpr.sept.so2.modules.coordenacao.application.CursoConfigAcesso
import br.ufpr.sept.so2.modules.coordenacao.application.ObterConfigCursoUseCase
import br.ufpr.sept.so2.modules.coordenacao.domain.ConfigCursoPatch
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/coordenacao/cursos")
@Tag(name = "Coordenação", description = "Parâmetros curriculares do curso (RF-F6-001)")
class ConfigCursoController(
    private val obterConfigCursoUseCase: ObterConfigCursoUseCase,
    private val atualizarConfigCursoUseCase: AtualizarConfigCursoUseCase,
) {
    @GetMapping("/{id}/config")
    @PreAuthorize("hasAuthority('course.config')")
    @Operation(summary = "Ler configuração do curso do coordenador")
    fun obter(@PathVariable id: UUID, authentication: Authentication): ConfigCursoResponse {
        val principal = principal(authentication)
        val config = obterConfigCursoUseCase.execute(id, principal.userId, principal.authorities)
        return ConfigCursoResponse.from(config, podeAtualizar(principal))
    }

    @PatchMapping("/{id}/config")
    @PreAuthorize("hasAuthority('course.config')")
    @Operation(summary = "Atualizar configuração do curso do coordenador")
    fun atualizar(
        @PathVariable id: UUID,
        @RequestBody request: ConfigCursoRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): ConfigCursoResponse {
        val principal = principal(authentication)
        val config = atualizarConfigCursoUseCase.execute(
            id,
            principal.userId,
            principal.authorities,
            request.toPatch(),
            clientIp(http),
        )
        return ConfigCursoResponse.from(config, podeAtualizar(principal))
    }

    private fun podeAtualizar(principal: IamPrincipal): Boolean =
        CursoConfigAcesso.CAP in principal.authorities

    private fun ConfigCursoRequest.toPatch() = ConfigCursoPatch(
        horasFormativasMinimas,
        duracaoCalendario,
        bancaMembrosExternos,
        bancaModalidade,
        regimento,
    )

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
