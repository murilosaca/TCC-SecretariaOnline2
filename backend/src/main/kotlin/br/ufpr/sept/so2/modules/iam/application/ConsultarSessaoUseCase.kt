package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ConsultarSessaoUseCase(
    private val usuarioRepository: UsuarioRepository,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID): SessaoAtual {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
        return SessaoAtual(usuario.id, usuario.precisaPrimeiroAcesso(), usuario.authorities)
    }

    data class SessaoAtual(
        val id: UUID,
        val mustChangePassword: Boolean,
        val authorities: List<String>,
    )
}
