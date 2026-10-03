package br.ufpr.sept.so2.modules.comunicacao.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.comunicacao.application.ports.AudienciaPort
import br.ufpr.sept.so2.modules.comunicacao.application.ports.DestinatarioComunicacao
import br.ufpr.sept.so2.modules.comunicacao.application.ports.TurmaAudienciaPort
import br.ufpr.sept.so2.modules.comunicacao.domain.AudienciaOpcao
import br.ufpr.sept.so2.modules.comunicacao.domain.TipoAudiencia
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AudienciaAdapter(
    private val turmaPort: TurmaAudienciaPort,
    private val cursoRepository: CursoRepository,
    private val alunoRepository: AlunoRepository,
    private val usuarioRepository: UsuarioRepository,
) : AudienciaPort {
    override fun opcoes(professorId: UUID): List<AudienciaOpcao> {
        val turmas = turmaPort.findByProfessor(professorId).map {
            AudienciaOpcao(TipoAudiencia.TURMA, it.id, "Turma ${it.nome}")
        }
        val cursos = cursoRepository.findIdsByCoordenador(professorId).mapNotNull { cursoId ->
            cursoRepository.findById(cursoId).orElse(null)?.let { curso ->
                AudienciaOpcao(TipoAudiencia.CURSO, curso.id, "Curso ${curso.sigla}")
            }
        }.sortedBy { it.rotulo }
        return turmas + cursos
    }

    override fun professorPode(professorId: UUID, tipo: TipoAudiencia, audienciaId: UUID): Boolean =
        when (tipo) {
            TipoAudiencia.TURMA -> turmaPort.findByIdEProfessor(audienciaId, professorId) != null
            TipoAudiencia.CURSO -> cursoRepository.findById(audienciaId).orElse(null)?.idCoordenador == professorId
        }

    override fun destinatarios(tipo: TipoAudiencia, audienciaId: UUID, autorId: UUID): List<DestinatarioComunicacao> {
        val alunos = when (tipo) {
            TipoAudiencia.TURMA -> turmaPort.alunoIds(audienciaId).mapNotNull { alunoRepository.findById(it).orElse(null) }
            TipoAudiencia.CURSO -> alunoRepository.findByIdCursoIn(listOf(audienciaId))
        }
        return alunos
            .filter { elegivel(it) }
            .mapNotNull { aluno -> usuarioDe(aluno, autorId) }
            .distinctBy { it.usuarioId }
    }

    private fun elegivel(aluno: Aluno): Boolean =
        aluno.ativo && aluno.situacao in SITUACOES

    private fun usuarioDe(aluno: Aluno, autorId: UUID): DestinatarioComunicacao? {
        val usuario = usuarioRepository.findByEmail(aluno.emailInstitucional.value).orElse(null) ?: return null
        if (!usuario.ativo || usuario.id == autorId) {
            return null
        }
        return DestinatarioComunicacao(usuario.id, usuario.emailInstitucional.value)
    }

    companion object {
        private val SITUACOES = setOf(AlunoSituacao.MATRICULADO, AlunoSituacao.FORMANDO)
    }
}
