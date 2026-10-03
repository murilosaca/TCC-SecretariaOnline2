package br.ufpr.sept.so2.modules.atendimentos.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.atendimentos.application.ports.AlunoAtendimentoPort
import br.ufpr.sept.so2.modules.atendimentos.application.ports.AlunoAtendimentoPort.Cadastro
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class AlunoAtendimentoAdapter(
    private val usuarioRepository: UsuarioRepository,
    private val alunoRepository: AlunoRepository,
) : AlunoAtendimentoPort {

    @Transactional(readOnly = true)
    override fun cadastro(alunoId: UUID): Cadastro? =
        alunoRepository.findById(alunoId).map(::toCadastro).orElse(null)

    @Transactional(readOnly = true)
    override fun resolverPorUsuario(usuarioId: UUID): Cadastro? {
        val usuario = usuarioRepository.findById(usuarioId).orElse(null) ?: return null
        val porGrr = usuario.grr?.let { alunoRepository.findByGrr(it.value).orElse(null) }
        val aluno = porGrr
            ?: alunoRepository.findByEmailInstitucional(usuario.emailInstitucional.value).orElse(null)
            ?: return null
        return toCadastro(aluno)
    }

    companion object {
        private fun toCadastro(aluno: Aluno): Cadastro =
            Cadastro(aluno.id, aluno.nomeSocial ?: aluno.nome, aluno.grr.value, aluno.idCurso)
    }
}
