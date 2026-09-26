package br.ufpr.sept.so2.modules.iam.api.dto

import br.ufpr.sept.so2.modules.iam.domain.Usuario
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID

/** Resposta enxuta para o picker reusável (F5.7 e fatias seguintes). */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UsuarioOpcaoResponse(
    val id: UUID,
    val nome: String,
    val emailInstitucional: String,
    val grr: String?,
) {
    companion object {
        fun from(usuario: Usuario): UsuarioOpcaoResponse =
            UsuarioOpcaoResponse(
                id = usuario.id,
                nome = usuario.nome,
                emailInstitucional = usuario.emailInstitucional.value,
                grr = usuario.grr?.value,
            )
    }
}
