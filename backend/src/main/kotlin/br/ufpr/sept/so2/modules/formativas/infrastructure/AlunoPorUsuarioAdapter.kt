package br.ufpr.sept.so2.modules.formativas.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort.AlunoRef
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class AlunoPorUsuarioAdapter(
    private val usuarioRepository: UsuarioRepository,
    private val alunoRepository: AlunoRepository,
    private val cursoRepository: CursoRepository,
) : AlunoPorUsuarioPort {

    @Transactional(readOnly = true)
    override fun resolver(usuarioId: UUID): AlunoRef? {
        val usuario = usuarioRepository.findById(usuarioId).orElse(null) ?: return null
        val porGrr = usuario.grr?.let { alunoRepository.findByGrr(it.value).orElse(null) }
        val aluno = porGrr
            ?: usuario.emailInstitucional.let { alunoRepository.findByEmailInstitucional(it.value).orElse(null) }
            ?: return null
        val horas = cursoRepository.findById(aluno.idCurso)
            .map { it.horasFormativasMinimas }
            .orElse(0)
        return AlunoRef(aluno.id, aluno.situacao == AlunoSituacao.EGRESSO, horas)
    }
}
