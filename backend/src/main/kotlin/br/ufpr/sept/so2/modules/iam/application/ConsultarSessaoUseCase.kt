package br.ufpr.sept.so2.modules.iam.application

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
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID): SessaoAtual {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
        val cursoConfigurarId = if (usuario.authorities.contains("course.config")) {
            coordenadorCursosPort.ids(usuario.id).singleOrNull()
        } else {
            null
        }
        return SessaoAtual(
            usuario.id,
            usuario.precisaPrimeiroAcesso(),
            usuario.authorities,
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
