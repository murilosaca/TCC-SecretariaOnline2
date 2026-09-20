package br.ufpr.sept.so2.modules.academico.api.dto

import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class AlunoResponse(
    val id: UUID,
    val nome: String,
    val nomeSocial: String?,
    val grr: String,
    val emailInstitucional: String,
    val emailPessoal: String?,
    val telefone: String?,
    val idCurso: UUID,
    val situacao: AlunoSituacao,
    val ativo: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun from(aluno: Aluno, podeGerenciar: Boolean): AlunoResponse {
            val base = "/academico/alunos/${aluno.id}"
            val links = linkedMapOf("self" to base)
            if (podeGerenciar) {
                links["atualizar"] = base
                links["excluir"] = base
            }
            return AlunoResponse(
                id = aluno.id,
                nome = aluno.nome,
                nomeSocial = aluno.nomeSocial,
                grr = aluno.grr.value,
                emailInstitucional = aluno.emailInstitucional.value,
                emailPessoal = aluno.emailPessoal?.value,
                telefone = aluno.telefone,
                idCurso = aluno.idCurso,
                situacao = aluno.situacao,
                ativo = aluno.ativo,
                createdAt = aluno.createdAt,
                updatedAt = aluno.updatedAt,
                links = links,
            )
        }
    }
}
