package br.ufpr.sept.so2.modules.solicitacoes.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitanteResumo
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitanteResumoPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class SolicitanteResumoAdapter(
    private val usuarioRepository: UsuarioRepository,
    private val alunoRepository: AlunoRepository,
    private val cursoRepository: CursoRepository,
) : SolicitanteResumoPort {

    @Transactional(readOnly = true)
    override fun nomeDe(usuarioId: UUID): String? = resumoDe(usuarioId).nome

    @Transactional(readOnly = true)
    override fun resumoDe(usuarioId: UUID): SolicitanteResumo {
        val usuario = usuarioRepository.findById(usuarioId).orElse(null)
            ?: return SolicitanteResumo(null, null, null, null, null)
        val porGrr = usuario.grr?.let { alunoRepository.findByGrr(it.value).orElse(null) }
        val aluno = porGrr
            ?: alunoRepository.findByEmailInstitucional(usuario.emailInstitucional.value).orElse(null)
        val nomeSocial = aluno?.nomeSocial?.trim().orEmpty()
        val nomeAluno = aluno?.nome?.trim().orEmpty()
        val nome = when {
            nomeSocial.isNotEmpty() -> nomeSocial
            nomeAluno.isNotEmpty() -> nomeAluno
            usuario.nome.isNotBlank() -> usuario.nome.trim()
            else -> usuario.emailInstitucional.value
        }
        val grr = aluno?.grr?.value ?: usuario.grr?.value
        val cursoId = aluno?.idCurso
        val curso = cursoId?.let { cursoRepository.findById(it).orElse(null) }
        return SolicitanteResumo(
            nome,
            grr,
            cursoId,
            curso?.nome,
            curso?.sigla,
            usuario.fotoStorageKey,
        )
    }
}
