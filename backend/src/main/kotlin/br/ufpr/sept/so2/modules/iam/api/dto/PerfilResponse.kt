package br.ufpr.sept.so2.modules.iam.api.dto

import br.ufpr.sept.so2.modules.iam.application.PerfilVisao
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class PerfilResponse(
    val id: UUID,
    val nome: String,
    val nomeSocial: String?,
    val telefone: String?,
    val emailPessoal: String?,
    val emailInstitucional: String,
    val grr: String?,
    val identidadeGenero: String?,
    val fotoUrl: String?,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun from(visao: PerfilVisao): PerfilResponse {
            val usuario = visao.usuario
            return PerfilResponse(
                usuario.id,
                usuario.nome,
                usuario.nomeSocial,
                usuario.telefone,
                usuario.emailPessoal?.value,
                usuario.emailInstitucional.value,
                usuario.grr?.value,
                usuario.identidadeGenero,
                visao.fotoUrl,
                mapOf(
                    "self" to "/me",
                    "update" to "/me",
                    "foto" to "/me/foto",
                ),
            )
        }
    }
}

data class TrocarSenhaResponse(
    val mensagem: String,
) {
    companion object {
        const val MENSAGEM: String = "Senha alterada. Outras sessões foram encerradas."
    }
}
