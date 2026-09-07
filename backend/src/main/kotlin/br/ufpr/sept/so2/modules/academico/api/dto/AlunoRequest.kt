package br.ufpr.sept.so2.modules.academico.api.dto

import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class AlunoRequest(
    @field:NotBlank @field:Size(max = 200) val nome: String? = null,
    @field:Size(max = 200) val nomeSocial: String? = null,
    @field:NotBlank val grr: String? = null,
    @field:NotBlank val emailInstitucional: String? = null,
    val emailPessoal: String? = null,
    @field:Size(max = 30) val telefone: String? = null,
    @field:NotNull val idCurso: UUID? = null,
    val situacao: AlunoSituacao? = null,
    val ativo: Boolean? = null,
)
