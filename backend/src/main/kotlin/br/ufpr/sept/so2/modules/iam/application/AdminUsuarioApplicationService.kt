package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminUsuarioApplicationService(
    private val usuarioRepository: UsuarioRepository,
) {
    @Transactional(readOnly = true)
    fun listar(termo: String?, pageable: Pageable): Page<Usuario> =
        usuarioRepository.search(termo, pageable)

    @Transactional(readOnly = true)
    fun buscar(id: UUID): Usuario =
        usuarioRepository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
}
