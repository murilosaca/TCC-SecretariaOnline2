package br.ufpr.sept.so2.modules.atendimentos.api

import br.ufpr.sept.so2.modules.atendimentos.api.dto.AnexoAtendimentoResponse
import br.ufpr.sept.so2.modules.atendimentos.api.dto.AtendimentoResponse
import br.ufpr.sept.so2.modules.atendimentos.api.dto.CategoriasAtendimentoResponse
import br.ufpr.sept.so2.modules.atendimentos.application.AtendimentoItem
import br.ufpr.sept.so2.modules.atendimentos.application.BaixarAnexoAtendimentoUseCase
import br.ufpr.sept.so2.modules.atendimentos.application.DarCienciaAtendimentoUseCase
import br.ufpr.sept.so2.modules.atendimentos.application.ListarAtendimentosDoAlunoUseCase
import br.ufpr.sept.so2.modules.atendimentos.application.ListarCategoriasAtendimentoUseCase
import br.ufpr.sept.so2.modules.atendimentos.application.RegistrarAtendimentoUseCase
import br.ufpr.sept.so2.modules.iam.api.ClienteIp
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/atendimentos")
@Tag(name = "Atendimentos", description = "Registro imutável e ciência do aluno (RF-F5-007 / RF-F1-011)")
class AtendimentoController(
    private val registrarAtendimentoUseCase: RegistrarAtendimentoUseCase,
    private val listarAtendimentosDoAlunoUseCase: ListarAtendimentosDoAlunoUseCase,
    private val listarCategoriasAtendimentoUseCase: ListarCategoriasAtendimentoUseCase,
    private val darCienciaAtendimentoUseCase: DarCienciaAtendimentoUseCase,
    private val baixarAnexoAtendimentoUseCase: BaixarAnexoAtendimentoUseCase,
    private val assembler: AtendimentoAssembler,
) {

    @GetMapping("/categorias")
    @PreAuthorize("hasAuthority('service_record.create')")
    @Operation(summary = "Categorias de atendimento do seed (sem CRUD de admin)")
    fun categorias(authentication: Authentication): CategoriasAtendimentoResponse =
        assembler.categorias(listarCategoriasAtendimentoUseCase.execute(), principal(authentication).authorities)

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAuthority('service_record.create')")
    @Operation(summary = "Registrar atendimento presencial (anexo PDF opcional, até 10 MB)")
    fun registrar(
        @RequestParam(required = false) alunoId: String?,
        @RequestParam(required = false) categoriaId: String?,
        @RequestParam(required = false) assunto: String?,
        @RequestParam(required = false) resposta: String?,
        @RequestParam(required = false) anexo: MultipartFile?,
        authentication: Authentication,
        http: HttpServletRequest,
    ): ResponseEntity<AtendimentoResponse> {
        val principal = principal(authentication)
        val registrado = registrarAtendimentoUseCase.execute(
            principal.userId,
            uuid(alunoId),
            uuid(categoriaId),
            assunto,
            resposta,
            anexo?.takeIf { !it.isEmpty }?.bytes,
            ClienteIp.de(http),
        )
        val item = AtendimentoItem(registrado.atendimento, registrado.categoria.nome)
        val corpo = assembler.from(item, principal.authorities)
        return ResponseEntity.created(URI.create("/atendimentos/${registrado.atendimento.id}")).body(corpo)
    }

    @GetMapping
    @PreAuthorize("hasAuthority('service_record.view_own')")
    @Operation(summary = "Atendimentos do aluno autenticado (filtro opcional por status)")
    fun listar(
        @RequestParam(defaultValue = "me") aluno: String,
        @RequestParam(required = false) status: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<AtendimentoResponse> {
        val principal = principal(authentication)
        return PageResponse.ofWithLinks(
            listarAtendimentosDoAlunoUseCase.execute(principal.userId, aluno, status, pageable),
            Function { item -> assembler.from(item, principal.authorities) },
        )
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAuthority('service_record.view_own')")
    @Operation(summary = "Dar ciência (PENDENTE_CIENCIA → CIENCIA_DADA)")
    fun darCiencia(
        @PathVariable id: UUID,
        authentication: Authentication,
        http: HttpServletRequest,
    ): AtendimentoResponse {
        val principal = principal(authentication)
        val item = darCienciaAtendimentoUseCase.execute(id, principal.userId, ClienteIp.de(http))
        return assembler.from(item, principal.authorities)
    }

    @GetMapping("/{id}/anexo")
    @PreAuthorize("hasAuthority('service_record.view_own')")
    @Operation(summary = "URL pré-assinada do anexo do próprio atendimento (TTL 15 min)")
    fun anexo(@PathVariable id: UUID, authentication: Authentication): AnexoAtendimentoResponse {
        val arquivo = baixarAnexoAtendimentoUseCase.execute(id, principal(authentication).userId)
        return AnexoAtendimentoResponse(arquivo.nomeArquivo, arquivo.downloadUrl, arquivo.expiresInSeconds)
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal

        private fun uuid(raw: String?): UUID? {
            if (raw.isNullOrBlank()) {
                return null
            }
            try {
                return UUID.fromString(raw.trim())
            } catch (_: IllegalArgumentException) {
                throw DadoInvalidoException("Identificador inválido.")
            }
        }
    }
}
