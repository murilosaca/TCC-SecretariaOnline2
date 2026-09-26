package br.ufpr.sept.so2.modules.formativas.infrastructure

import br.ufpr.sept.so2.modules.formativas.application.ports.AutorFormativaPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class AutorFormativaAdapter(
    private val usuarioRepository: UsuarioRepository,
) : AutorFormativaPort {

    @Transactional(readOnly = true)
    override fun rotulo(usuarioId: UUID): String? =
        usuarioRepository.findById(usuarioId).map { it.emailInstitucional.value }.orElse(null)
}
