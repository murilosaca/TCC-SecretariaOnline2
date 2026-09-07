package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "aluno")
class AlunoJpaEntity : BaseEntity() {
    @Column(nullable = false, length = 200)
    var nome: String = ""

    @Column(name = "nome_social", length = 200)
    var nomeSocial: String? = null

    @Column(nullable = false, unique = true, length = 11)
    var grr: String = ""

    @Column(name = "email_institucional", nullable = false, unique = true, columnDefinition = "citext")
    var emailInstitucional: String = ""

    @Column(name = "email_pessoal", columnDefinition = "citext")
    var emailPessoal: String? = null

    @Column(length = 30)
    var telefone: String? = null

    @Column(name = "id_curso", nullable = false)
    var idCurso: UUID? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var situacao: AlunoSituacao = AlunoSituacao.MATRICULADO

    @Column(nullable = false)
    var ativo: Boolean = false

    fun merge(aluno: Aluno) {
        nome = aluno.nome
        nomeSocial = aluno.nomeSocial
        grr = aluno.grr.value
        emailInstitucional = aluno.emailInstitucional.value
        emailPessoal = aluno.emailPessoal?.value
        telefone = aluno.telefone
        idCurso = aluno.idCurso
        situacao = aluno.situacao
        ativo = aluno.ativo
    }

    fun toDomain() = Aluno(
        requireNotNull(id),
        nome,
        nomeSocial,
        Grr.of(grr),
        Email.of(emailInstitucional),
        emailPessoal?.let { Email.of(it) },
        telefone,
        requireNotNull(idCurso),
        situacao,
        ativo,
        requireNotNull(createdAt),
        requireNotNull(updatedAt),
    )

    companion object {
        fun fromDomain(aluno: Aluno) = AlunoJpaEntity().apply {
            id = aluno.id
            merge(aluno)
        }
    }
}
