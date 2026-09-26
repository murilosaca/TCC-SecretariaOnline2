package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.bff.application.ports.ProfessorIdentidadeQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.ProfessorIdentidadeQueryPort.ProfessorIdentidade
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class ProfessorIdentidadeQueryAdapter(
    private val usuarioRepository: UsuarioRepository,
) : ProfessorIdentidadeQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(usuarioId: UUID): ProfessorIdentidade {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RecursoNaoEncontradoException("Usuário autenticado não encontrado.") }
        val nome = usuario.nome.trim().ifBlank { nomeDoEmail(usuario.emailInstitucional.value) }
        return ProfessorIdentidade(nome)
    }

    companion object {
        private fun nomeDoEmail(email: String): String {
            val local = email.substringBefore('@').replace('.', ' ').trim()
            if (local.isEmpty()) {
                return "Professor"
            }
            return local.split(Regex("\\s+"))
                .filter { it.isNotEmpty() }
                .joinToString(" ") { parte ->
                    parte.replaceFirstChar { it.uppercaseChar() }
                }
        }
    }
}
