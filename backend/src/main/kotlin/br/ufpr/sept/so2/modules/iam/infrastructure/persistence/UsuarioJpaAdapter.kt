package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    override fun findAtivosByAuthority(authority: String): List<Usuario> =
        jpaRepository.findAtivosByAuthority(authority).map { it.toDomain() }

    override fun search(termo: String?, pageable: Pageable): Page<Usuario> {
        val limpo = termo?.trim()?.takeIf { it.isNotEmpty() }
        return jpaRepository.search(limpo, pageable).map { it.toDomain() }
    }

    override fun existsById(id: UUID): Boolean = jpaRepository.existsById(id)
}
