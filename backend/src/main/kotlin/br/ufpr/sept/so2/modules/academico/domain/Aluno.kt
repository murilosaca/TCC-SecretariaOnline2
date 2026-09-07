package br.ufpr.sept.so2.modules.academico.domain

import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import java.time.OffsetDateTime
import java.util.UUID

class Aluno(
    val id: UUID,
    var nome: String,
    var nomeSocial: String?,
    var grr: Grr,
    var emailInstitucional: Email,
    var emailPessoal: Email?,
    var telefone: String?,
    var idCurso: UUID,
    var situacao: AlunoSituacao,
    @get:JvmName("isAtivo")
    var ativo: Boolean,
    val createdAt: OffsetDateTime,
    var updatedAt: OffsetDateTime,
) {
    fun atualizar(
        nome: String?,
        nomeSocial: String?,
        emailPessoal: Email?,
        telefone: String?,
        idCurso: UUID?,
        situacao: AlunoSituacao?,
        ativo: Boolean?,
    ) {
        if (nome != null) {
            this.nome = nome
        }
        this.nomeSocial = nomeSocial
        this.emailPessoal = emailPessoal
        this.telefone = telefone
        if (idCurso != null) {
            this.idCurso = idCurso
        }
        if (situacao != null) {
            this.situacao = situacao
        }
        if (ativo != null) {
            this.ativo = ativo
        }
        this.updatedAt = OffsetDateTime.now()
    }
}
