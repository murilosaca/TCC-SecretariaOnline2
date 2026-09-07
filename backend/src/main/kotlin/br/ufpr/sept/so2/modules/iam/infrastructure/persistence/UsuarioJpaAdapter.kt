package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class UsuarioJpaAdapter(
    private val jpaRepository: UsuarioJpaRepository,
) : UsuarioRepository {
    override fun save(usuario: Usuario): Usuario {
        val entity = jpaRepository.findById(usuario.id)
            .orElseGet { UsuarioJpaEntity.fromDomain(usuario) }
        entity.merge(usuario)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Optional<Usuario> =
        jpaRepository.findById(id).map { it.toDomain() }

    override fun findByIdentificador(identificador: IdentificadorLogin): Optional<Usuario> {
        if (identificador.tipo == IdentificadorLogin.Tipo.GRR) {
            return jpaRepository.findByGrrIgnoreCase(identificador.valor).map { it.toDomain() }
        }
        return findByEmail(identificador.valor)
    }

    override fun findByEmail(email: String): Optional<Usuario> =
        jpaRepository.findByAnyEmail(email).map { it.toDomain() }
}
