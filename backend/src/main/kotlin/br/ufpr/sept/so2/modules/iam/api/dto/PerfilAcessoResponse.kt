package br.ufpr.sept.so2.modules.iam.api.dto

import br.ufpr.sept.so2.modules.iam.application.AuthorityResumo
import br.ufpr.sept.so2.modules.iam.application.PerfilResumo
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class PerfilAcessoResponse(
    val id: UUID,
    val nome: String,
    val descricao: String?,
    val tipo: String,
    val authorities: List<String>,
    val quantidadeAuthorities: Int,
    val quantidadeUsuarios: Int,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun from(perfil: PerfilResumo): PerfilAcessoResponse {
            val base = "/admin/perfis/${perfil.id}"
            val links = linkedMapOf("self" to base, "atualizar" to base)
            if (perfil.tipo != "SYSTEM") {
                links["excluir"] = base
            }
            return PerfilAcessoResponse(
                perfil.id,
                perfil.nome,
                perfil.descricao,
                perfil.tipo,
                perfil.authorities,
                perfil.authorities.size,
                perfil.usuariosAtivos,
                links,
            )
        }
    }
}

data class PerfilRequest(
    val nome: String? = null,
    val descricao: String? = null,
    val authorities: List<String> = emptyList(),
)

data class AuthorityResponse(
    val nome: String,
    val descricao: String,
    val modulo: String,
    val sistema: Boolean,
    val readOnlyName: Boolean,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun from(authority: AuthorityResumo): AuthorityResponse =
            AuthorityResponse(
                authority.nome,
                authority.descricao,
                authority.modulo,
                authority.sistema,
                authority.sistema,
                mapOf("atualizar" to "/admin/autoridades/${authority.nome}"),
            )
    }
}

data class AuthorityDescricaoRequest(val descricao: String? = null)

data class MatrizResponse(
    val authorities: List<AuthorityResponse>,
    val perfis: List<PerfilAcessoResponse>,
    val matriz: Map<String, List<String>>,
)

data class MatrizRequest(val perfis: List<MatrizPerfilRequest> = emptyList())

data class MatrizPerfilRequest(
    val id: UUID? = null,
    val authorities: List<String> = emptyList(),
)

data class AtribuicaoPerfisResponse(
    val usuarioId: UUID,
    val selecionados: List<UUID>,
    val perfis: List<PerfilAcessoResponse>,
)

data class AtribuicaoPerfisRequest(val perfilIds: List<UUID> = emptyList())
