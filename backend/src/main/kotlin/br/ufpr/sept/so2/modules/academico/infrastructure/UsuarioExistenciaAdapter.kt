package br.ufpr.sept.so2.modules.academico.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.UsuarioExistenciaPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UsuarioExistenciaAdapter(
    private val usuarioRepository: UsuarioRepository,
) : UsuarioExistenciaPort {
    override fun existe(id: UUID): Boolean = usuarioRepository.existsById(id)

    override fun existemTodos(ids: Collection<UUID>): Boolean {
        if (ids.isEmpty()) {
            return true
        }
        return ids.all { usuarioRepository.existsById(it) }
    }
}
