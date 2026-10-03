package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuthoritiesDePerfilPort
import br.ufpr.sept.so2.modules.iam.application.ports.CoordenadorCursosPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ConsultarSessaoUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val coordenadorCursosPort: CoordenadorCursosPort,
    private val authoritiesDePerfilPort: AuthoritiesDePerfilPort,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID): SessaoAtual {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
        val authorities = authoritiesDePerfilPort.uniao(usuario.id) ?: usuario.authorities
        val cursoConfigurarId = if (authorities.contains("course.config")) {
            coordenadorCursosPort.ids(usuario.id).singleOrNull()
        } else {
            null
        }
        return SessaoAtual(
            usuario.id,
            usuario.precisaPrimeiroAcesso(),
            authorities,
            cursoConfigurarId,
        )
    }

    data class SessaoAtual(
        val id: UUID,
        val mustChangePassword: Boolean,
        val authorities: List<String>,
        val cursoConfigurarId: UUID? = null,
    )
}
