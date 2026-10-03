package br.ufpr.sept.so2.modules.busca.api

import br.ufpr.sept.so2.modules.busca.application.BuscaHit
import br.ufpr.sept.so2.modules.busca.application.BuscarGlobalUseCase
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/search")
@Tag(name = "Busca", description = "F8.1 — paleta global filtrada por capability")
class BuscaController(
    private val buscarGlobal: BuscarGlobalUseCase,
) {
    @GetMapping
    fun buscar(
        @RequestParam(name = "q", defaultValue = "") q: String,
        authentication: Authentication,
    ): BuscaResponse {
        val principal = authentication.principal as IamPrincipal
        val resultado = buscarGlobal.execute(principal.userId, principal.authorities, q)
        return BuscaResponse(
            resultado.q,
            resultado.alunos.map { item(it) },
            resultado.solicitacoes.map { item(it) },
            resultado.eventos.map { item(it) },
            resultado.usuarios.map { item(it) },
        )
    }

    private fun item(hit: BuscaHit) = BuscaHitResponse(hit.id, hit.titulo, hit.subtitulo, hit.href)
}

data class BuscaHitResponse(
    val id: UUID,
    val titulo: String,
    val subtitulo: String,
    val href: String,
)

data class BuscaResponse(
    val q: String,
    val alunos: List<BuscaHitResponse>,
    val solicitacoes: List<BuscaHitResponse>,
    val eventos: List<BuscaHitResponse>,
    val usuarios: List<BuscaHitResponse>,
)
