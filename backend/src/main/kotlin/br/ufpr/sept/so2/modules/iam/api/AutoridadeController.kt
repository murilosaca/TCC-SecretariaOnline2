package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.api.dto.AtribuicaoPerfisRequest
import br.ufpr.sept.so2.modules.iam.api.dto.AtribuicaoPerfisResponse
import br.ufpr.sept.so2.modules.iam.api.dto.AuthorityDescricaoRequest
import br.ufpr.sept.so2.modules.iam.api.dto.AuthorityResponse
import br.ufpr.sept.so2.modules.iam.api.dto.MatrizRequest
import br.ufpr.sept.so2.modules.iam.api.dto.MatrizResponse
import br.ufpr.sept.so2.modules.iam.api.dto.PerfilAcessoResponse
import br.ufpr.sept.so2.modules.iam.application.ports.PerfilAcessoPort
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@Tag(name = "Admin · Autoridades", description = "F7.3 — authorities e matriz perfil × authority")
class AutoridadeController(
    private val perfilAcessoPort: PerfilAcessoPort,
) {
    @GetMapping("/admin/autoridades")
    @PreAuthorize("hasAuthority('iam.manage_authorities')")
    @Operation(summary = "Listar authorities e a matriz")
    fun listar(): MatrizResponse {
        val matriz = perfilAcessoPort.listarAutoridades()
        return MatrizResponse(
            matriz.authorities.map(AuthorityResponse::from),
            matriz.perfis.map(PerfilAcessoResponse::from),
            matriz.perfis.associate { it.id.toString() to it.authorities },
        )
    }

    @PatchMapping("/admin/autoridades/{nome:.+}")
    @PreAuthorize("hasAuthority('iam.manage_authorities')")
    @Operation(summary = "Editar só a descrição da authority")
    fun atualizarDescricao(
        @PathVariable nome: String,
        @RequestBody request: AuthorityDescricaoRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): AuthorityResponse {
        val descricao = request.descricao ?: throw DadoInvalidoException("Descrição é obrigatória.")
        return AuthorityResponse.from(
            perfilAcessoPort.atualizarDescricao(nome, descricao, principal(authentication).userId, http.remoteAddr),
        )
    }

    @PutMapping("/admin/autoridades/matriz")
    @PreAuthorize("hasAuthority('iam.manage_authorities')")
    @Operation(summary = "Substituir o conjunto de authorities de cada perfil")
    fun salvarMatriz(
        @RequestBody request: MatrizRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): MatrizResponse {
        val vinculos = request.perfis.associate { item ->
            val id = item.id ?: throw DadoInvalidoException("Perfil da matriz sem id.")
            id to item.authorities
        }
        perfilAcessoPort.salvarMatriz(vinculos, principal(authentication).userId, http.remoteAddr)
        return listar()
    }

    @GetMapping("/admin/usuarios/{id}/perfis")
    @PreAuthorize("hasAuthority('iam.manage_roles')")
    @Operation(summary = "Perfis atribuídos ao usuário")
    fun atribuicao(@PathVariable id: UUID): AtribuicaoPerfisResponse {
        val atual = perfilAcessoPort.atribuicaoDe(id)
        return AtribuicaoPerfisResponse(
            atual.usuarioId,
            atual.selecionados,
            atual.perfis.map(PerfilAcessoResponse::from),
        )
    }

    @PutMapping("/admin/usuarios/{id}/perfis")
    @PreAuthorize("hasAuthority('iam.manage_roles')")
    @Operation(summary = "Substituir os perfis do usuário")
    fun substituir(
        @PathVariable id: UUID,
        @RequestBody request: AtribuicaoPerfisRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): AtribuicaoPerfisResponse {
        val atual = perfilAcessoPort.substituirPerfis(
            id,
            request.perfilIds,
            principal(authentication).userId,
            http.remoteAddr,
        )
        return AtribuicaoPerfisResponse(
            atual.usuarioId,
            atual.selecionados,
            atual.perfis.map(PerfilAcessoResponse::from),
        )
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}
