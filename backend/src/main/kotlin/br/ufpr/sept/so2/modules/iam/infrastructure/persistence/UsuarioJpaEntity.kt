package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.HashSet

@Entity
@Table(name = "usuario")
class UsuarioJpaEntity : BaseEntity() {
    @Column(name = "email_institucional", nullable = false, unique = true, columnDefinition = "citext")
    var emailInstitucional: String? = null

    @Column(name = "email_pessoal", unique = true, columnDefinition = "citext")
    var emailPessoal: String? = null

    @Column(unique = true, length = 11)
    var grr: String? = null

    @Column(name = "senha_hash", nullable = false, length = 255)
    var senhaHash: String? = null

    @Column(name = "senha_alterada", nullable = false)
    var senhaAlterada: Boolean = false

    @Column(name = "lgpd_aceite_em")
    var lgpdAceiteEm: OffsetDateTime? = null

    @Column(name = "lgpd_aceite_ip", length = 64)
    var lgpdAceiteIp: String? = null

    @Column(name = "lgpd_aceite_user_agent", length = 300)
    var lgpdAceiteUserAgent: String? = null

    @Column(nullable = false)
    var ativo: Boolean = false

    @Column(name = "falhas_consecutivas", nullable = false)
    var falhasConsecutivas: Int = 0

    @Column(name = "bloqueado_ate")
    var bloqueadoAte: OffsetDateTime? = null

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_authority", joinColumns = [JoinColumn(name = "usuario_id")])
    @Column(name = "authority", nullable = false, length = 80)
    var authorities: MutableSet<String> = HashSet()

    fun merge(usuario: Usuario) {
        emailInstitucional = usuario.emailInstitucional.value
        emailPessoal = usuario.emailPessoal?.value
        grr = usuario.grr?.value
        senhaHash = usuario.senhaHash
        senhaAlterada = usuario.senhaAlterada
        lgpdAceiteEm = usuario.lgpdAceiteEm
        lgpdAceiteIp = usuario.lgpdAceiteIp
        lgpdAceiteUserAgent = usuario.lgpdAceiteUserAgent
        ativo = usuario.ativo
        falhasConsecutivas = usuario.falhasConsecutivas
        bloqueadoAte = usuario.bloqueadoAte
        authorities = HashSet(usuario.authorities)
    }

    fun toDomain(): Usuario =
        Usuario(
            id!!,
            Email.of(emailInstitucional),
            if (emailPessoal == null) null else Email.of(emailPessoal),
            if (grr == null) null else Grr.of(grr),
            senhaHash!!,
            senhaAlterada,
            lgpdAceiteEm,
            lgpdAceiteIp,
            lgpdAceiteUserAgent,
            ativo,
            falhasConsecutivas,
            bloqueadoAte,
            authorities.sorted(),
            createdAt!!,
            updatedAt!!,
        )

    companion object {
        fun fromDomain(usuario: Usuario): UsuarioJpaEntity {
            val entity = UsuarioJpaEntity()
            entity.id = usuario.id
            entity.merge(usuario)
            return entity
        }
    }
}
