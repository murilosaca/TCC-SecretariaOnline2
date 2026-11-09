package br.ufpr.sept.so2.modules.comunicacao.infrastructure

import br.ufpr.sept.so2.modules.comunicacao.application.ports.EmailUsuarioPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class EmailUsuarioAdapter(
    private val usuarioRepository: UsuarioRepository,
) : EmailUsuarioPort {
    override fun emailDe(usuarioId: UUID): String? =
        usuarioRepository.findById(usuarioId).orElse(null)?.emailInstitucional?.value
}
