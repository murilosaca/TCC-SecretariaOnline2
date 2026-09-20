package br.ufpr.sept.so2.modules.solicitacoes.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitanteResumoPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class SolicitanteResumoAdapter(
    private val usuarioRepository: UsuarioRepository,
    private val alunoRepository: AlunoRepository,
) : SolicitanteResumoPort {

    @Transactional(readOnly = true)
    override fun nomeDe(usuarioId: UUID): String? {
        val usuario = usuarioRepository.findById(usuarioId).orElse(null) ?: return null
        val porGrr = usuario.grr?.let { alunoRepository.findByGrr(it.value).orElse(null) }
        val aluno = porGrr
            ?: alunoRepository.findByEmailInstitucional(usuario.emailInstitucional.value).orElse(null)
        val nomeSocial = aluno?.nomeSocial?.trim().orEmpty()
        if (nomeSocial.isNotEmpty()) {
            return nomeSocial
        }
        val nome = aluno?.nome?.trim().orEmpty()
        if (nome.isNotEmpty()) {
            return nome
        }
        return usuario.emailInstitucional.value
    }
}
