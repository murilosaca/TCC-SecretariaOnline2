package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.util.Objects
import java.util.UUID

@Embeddable
class UsuarioPerfilId(
    @Column(name = "usuario_id", nullable = false)
    var usuarioId: UUID? = null,
    @Column(name = "perfil_id", nullable = false)
    var perfilId: UUID? = null,
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is UsuarioPerfilId) {
            return false
        }
        return usuarioId == other.usuarioId && perfilId == other.perfilId
    }

    override fun hashCode(): Int = Objects.hash(usuarioId, perfilId)
}

@Entity
@Table(name = "usuario_perfil")
class UsuarioPerfilJpaEntity(
    @EmbeddedId
    var id: UsuarioPerfilId = UsuarioPerfilId(),
)
