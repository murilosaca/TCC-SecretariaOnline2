package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.bff.application.ports.AlunoIdentidadeQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.AlunoIdentidadeQueryPort.AlunoIdentidade
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import java.util.UUID

@Component
class AlunoIdentidadeQueryAdapter(
    private val usuarioRepository: UsuarioRepository,
    private val alunoRepository: AlunoRepository,
    private val cursoRepository: CursoRepository,
) : AlunoIdentidadeQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(usuarioId: UUID): AlunoIdentidade {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RecursoNaoEncontradoException("Usuário autenticado não encontrado.") }
        var aluno: Optional<Aluno> = Optional.empty()
        val grr = usuario.grr
        if (grr != null) {
            aluno = alunoRepository.findByGrr(grr.value)
        }
        val emailInstitucional = usuario.emailInstitucional
        if (aluno.isEmpty && emailInstitucional != null) {
            aluno = alunoRepository.findByEmailInstitucional(emailInstitucional.value)
        }
        val nome = aluno.map(::nomeExibicao).orElseGet { nomeDoEmail(emailInstitucional.value) }
        val curso = aluno
            .flatMap { encontrado -> cursoRepository.findById(encontrado.idCurso) }
            .map { item -> if (item.sigla.isNotBlank()) item.sigla else item.nome }
            .orElse(null)
        return AlunoIdentidade(nome, curso)
    }

    companion object {
        private fun nomeExibicao(aluno: Aluno): String {
            val social = aluno.nomeSocial
            if (!social.isNullOrBlank()) {
                return social.trim()
            }
            return aluno.nome
        }

        private fun nomeDoEmail(email: String): String {
            val local = email.substring(0, email.indexOf('@')).replace('.', ' ').trim()
            if (local.isEmpty()) {
                return "Aluno"
            }
            return local.split(Regex("\\s+"))
                .filter { it.isNotEmpty() }
                .joinToString(" ") { parte ->
                    parte.replaceFirstChar { it.uppercaseChar() }
                }
        }
    }
}
