package br.ufpr.sept.so2.modules.iam.api.dto

import br.ufpr.sept.so2.modules.iam.domain.Usuario
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AdminUsuarioResponse(
    val id: UUID,
    val nome: String,
    val emailInstitucional: String,
    val emailPessoal: String?,
    val grr: String?,
    val ativo: Boolean,
    val senhaAlterada: Boolean,
    val updatedAt: OffsetDateTime,
    @get:JsonProperty("_links")
    val links: Map<String, String>? = null,
) {
    companion object {
        fun from(
            usuario: Usuario,
            podeGerenciar: Boolean,
            podeReset: Boolean,
        ): AdminUsuarioResponse {
            val base = "/admin/usuarios/${usuario.id}"
            val links = linkedMapOf<String, String>()
            links["self"] = base
            if (podeGerenciar) {
                links["atualizar"] = base
                if (usuario.ativo) {
                    links["desativar"] = "$base/desativar"
                }
            }
            if (podeReset && usuario.ativo) {
                links["reset-senha"] = "$base/reset-senha"
            }
            return AdminUsuarioResponse(
                id = usuario.id,
                nome = usuario.nome,
                emailInstitucional = usuario.emailInstitucional.value,
                emailPessoal = usuario.emailPessoal?.value,
                grr = usuario.grr?.value,
                ativo = usuario.ativo,
                senhaAlterada = usuario.senhaAlterada,
                updatedAt = usuario.updatedAt,
                links = links,
            )
        }
    }
}
