package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.api.dto.PerfilResponse
import br.ufpr.sept.so2.modules.iam.api.dto.TrocarSenhaRequest
import br.ufpr.sept.so2.modules.iam.api.dto.TrocarSenhaResponse
import br.ufpr.sept.so2.modules.iam.application.AtualizarPerfilUseCase
import br.ufpr.sept.so2.modules.iam.application.CampoPatch
import br.ufpr.sept.so2.modules.iam.application.ConsultarPerfilUseCase
import br.ufpr.sept.so2.modules.iam.application.EnviarFotoPerfilUseCase
import br.ufpr.sept.so2.modules.iam.application.PerfilPatch
import br.ufpr.sept.so2.modules.iam.application.SessaoAtualResolver
import br.ufpr.sept.so2.modules.iam.application.TrocarSenhaUseCase
import br.ufpr.sept.so2.modules.iam.domain.FotoPerfilRegras
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/me")
@Tag(name = "Perfil", description = "Autogestão do usuário autenticado (F1.3–F1.4)")
class PerfilController(
    private val consultarPerfilUseCase: ConsultarPerfilUseCase,
    private val atualizarPerfilUseCase: AtualizarPerfilUseCase,
    private val enviarFotoPerfilUseCase: EnviarFotoPerfilUseCase,
    private val trocarSenhaUseCase: TrocarSenhaUseCase,
    private val sessaoAtualResolver: SessaoAtualResolver,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('user.update_own_profile')")
    @Operation(summary = "Dados pessoais do usuário autenticado")
    fun obter(authentication: Authentication): PerfilResponse =
        PerfilResponse.from(consultarPerfilUseCase.execute(principal(authentication).userId))

    @PatchMapping
    @PreAuthorize("hasAuthority('user.update_own_profile')")
    @Operation(summary = "Atualizar dados pessoais (merge parcial; GRR e e-mail institucional ignorados)")
    fun atualizar(
        @RequestBody body: JsonNode,
        authentication: Authentication,
        http: HttpServletRequest,
    ): PerfilResponse {
        val usuarioId = principal(authentication).userId
        atualizarPerfilUseCase.execute(usuarioId, patchDe(body), ClienteIp.de(http))
        return PerfilResponse.from(consultarPerfilUseCase.execute(usuarioId))
    }

    @PostMapping("/foto", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAuthority('user.update_own_profile')")
    @Operation(summary = "Enviar foto de perfil (upload server-side)")
    fun foto(
        @RequestParam("arquivo") arquivo: MultipartFile,
        authentication: Authentication,
        http: HttpServletRequest,
    ): PerfilResponse {
        if (arquivo.size > FotoPerfilRegras.MAX_BYTES) {
            throw DadoInvalidoException("A imagem deve ter no máximo 2 MB.")
        }
        val usuarioId = principal(authentication).userId
        enviarFotoPerfilUseCase.execute(usuarioId, arquivo.bytes, ClienteIp.de(http))
        return PerfilResponse.from(consultarPerfilUseCase.execute(usuarioId))
    }

    @PatchMapping("/password")
    @PreAuthorize("hasAuthority('user.update_own_profile')")
    @Operation(summary = "Trocar a senha e encerrar as outras sessões")
    fun senha(
        @Valid @RequestBody request: TrocarSenhaRequest,
        authentication: Authentication,
        @CookieValue(name = "\${app.iam.cookie-name:so2_refresh}", required = false) refreshCookie: String?,
        http: HttpServletRequest,
    ): TrocarSenhaResponse {
        val atual = principal(authentication)
        trocarSenhaUseCase.execute(
            atual.userId,
            request.senhaAtual,
            request.novaSenha,
            sessaoAtualResolver.resolver(atual.sessionId, refreshCookie, atual.userId),
            ClienteIp.de(http),
        )
        return TrocarSenhaResponse(TrocarSenhaResponse.MENSAGEM)
    }

    private fun patchDe(body: JsonNode): PerfilPatch {
        if (!body.isObject) {
            throw DadoInvalidoException("Corpo da requisição inválido.")
        }
        return PerfilPatch(
            nomeSocial = texto(body, "nomeSocial"),
            telefone = texto(body, "telefone"),
            emailPessoal = texto(body, "emailPessoal"),
            identidadeGenero = texto(body, "identidadeGenero"),
        )
    }

    private fun texto(body: JsonNode, campo: String): CampoPatch<String?> {
        if (!body.has(campo)) {
            return CampoPatch.ausente()
        }
        val node = body.get(campo)
        if (node.isNull) {
            return CampoPatch.de(null)
        }
        return CampoPatch.de(node.asText())
    }

    private fun principal(authentication: Authentication): IamPrincipal =
        authentication.principal as IamPrincipal
}
