package br.ufpr.sept.so2.modules.estagio.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort
import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort.AlunoRef
import br.ufpr.sept.so2.modules.estagio.application.ports.AutorEstagioPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class EstagioConsultaAdapter(
    private val usuarioRepository: UsuarioRepository,
    private val alunoRepository: AlunoRepository,
) : AlunoEstagioPort, AutorEstagioPort {

    @Transactional(readOnly = true)
    override fun resolver(usuarioId: UUID): AlunoRef? {
        val usuario = usuarioRepository.findById(usuarioId).orElse(null) ?: return null
        val porGrr = usuario.grr?.let { alunoRepository.findByGrr(it.value).orElse(null) }
        val aluno = porGrr
            ?: alunoRepository.findByEmailInstitucional(usuario.emailInstitucional.value).orElse(null)
            ?: return null
        return AlunoRef(aluno.id, aluno.situacao == AlunoSituacao.EGRESSO)
    }

    @Transactional(readOnly = true)
    override fun nomeDe(alunoId: UUID): String? =
        alunoRepository.findById(alunoId).map { it.nome }.orElse(null)

    @Transactional(readOnly = true)
    override fun cadastro(alunoId: UUID): AlunoEstagioPort.Cadastro? =
        alunoRepository.findById(alunoId)
            .map { AlunoEstagioPort.Cadastro(it.id, it.idCurso) }
            .orElse(null)

    @Transactional(readOnly = true)
    override fun rotulo(usuarioId: UUID): String? =
        usuarioRepository.findById(usuarioId).map { it.emailInstitucional.value }.orElse(null)
}
