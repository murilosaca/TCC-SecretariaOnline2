package br.ufpr.sept.so2.modules.iam.application.ports

import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import java.util.Optional
import java.util.UUID

interface UsuarioRepository {
    fun save(usuario: Usuario): Usuario

    fun findById(id: UUID): Optional<Usuario>

    fun findByIdentificador(identificador: IdentificadorLogin): Optional<Usuario>

    fun findByEmail(email: String): Optional<Usuario>
}
