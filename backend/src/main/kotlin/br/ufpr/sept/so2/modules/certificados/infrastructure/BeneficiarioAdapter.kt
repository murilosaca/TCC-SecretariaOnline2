package br.ufpr.sept.so2.modules.certificados.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.certificados.application.ports.BeneficiarioPort
import br.ufpr.sept.so2.modules.certificados.application.ports.BeneficiarioPort.BeneficiarioRef
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class BeneficiarioAdapter(
    private val usuarioRepository: UsuarioRepository,
    private val alunoRepository: AlunoRepository,
) : BeneficiarioPort {

    @Transactional(readOnly = true)
    override fun resolverPorUsuario(usuarioId: UUID): BeneficiarioRef? {
        val usuario = usuarioRepository.findById(usuarioId).orElse(null) ?: return null
        val porGrr = usuario.grr?.let { alunoRepository.findByGrr(it.value).orElse(null) }
        val aluno = porGrr
            ?: usuario.emailInstitucional.let { alunoRepository.findByEmailInstitucional(it.value).orElse(null) }
            ?: return null
        val nome = nomePublico(aluno) ?: return null
        return BeneficiarioRef(aluno.id, nome, aluno.situacao == AlunoSituacao.EGRESSO)
    }

    @Transactional(readOnly = true)
    override fun nomeDe(alunoId: UUID): String? {
        val aluno = alunoRepository.findById(alunoId).orElse(null) ?: return null
        return nomePublico(aluno)
    }

    private fun nomePublico(aluno: Aluno): String? {
        val social = aluno.nomeSocial?.trim().orEmpty()
        if (social.isNotEmpty()) {
            return social
        }
        val nome = aluno.nome.trim()
        return nome.ifEmpty { null }
    }
}
