package br.ufpr.sept.so2.modules.diplomas.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.coordenacao.application.ports.ElegibilidadeHorasPort
import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.diplomas.domain.AvaliarElegibilidadeColacao
import br.ufpr.sept.so2.modules.diplomas.domain.ElegibilidadeColacao
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.TccEstado
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class ListaElegiveisColacao(
    val cursoId: UUID,
    val periodoId: UUID,
    val itens: List<ElegibilidadeColacao>,
)

@Service
class ListarElegiveisColacaoUseCase(
    private val cursoRepository: CursoRepository,
    private val periodoLetivoRepository: PeriodoLetivoRepository,
    private val alunoRepository: AlunoRepository,
    private val diplomaRepository: DiplomaRepository,
    private val tccRepository: TccRepository,
    private val formativaRepository: FormativaRepository,
    private val elegibilidadeHorasPort: ElegibilidadeHorasPort,
) {
    @Transactional(readOnly = true)
    fun execute(cursoId: UUID, periodoId: UUID): ListaElegiveisColacao {
        val curso = cursoRepository.findById(cursoId)
            .orElseThrow { RecursoNaoEncontradoException("Curso não encontrado.") }
        periodoLetivoRepository.findById(periodoId)
            .orElseThrow { RecursoNaoEncontradoException("Período letivo não encontrado.") }

        val alunos = alunoRepository.findByIdCursoIn(listOf(cursoId))
            .filter { it.situacao != AlunoSituacao.EGRESSO && it.ativo }
            .filter { !diplomaRepository.existsByAluno(it.id) }
            .sortedBy { it.nome.lowercase() }

        val limiar = curso.horasFormativasMinimas
        val itens = alunos.map { aluno -> avaliar(aluno, limiar) }
        return ListaElegiveisColacao(cursoId, periodoId, itens)
    }

    private fun avaliar(aluno: Aluno, limiarAtual: Int): ElegibilidadeColacao {
        val tccAprovado = tccRepository
            .findByAluno(aluno.id, TccEstado.APROVADO, PageRequest.of(0, 1))
            .hasContent()
        val horas = formativaRepository.somarCargaHoraria(aluno.id, FormativaEstado.APROVADA)
        val requeridas = elegibilidadeHorasPort.requeridas(aluno.id, limiarAtual, horas)
        return AvaliarElegibilidadeColacao.avaliar(
            aluno.id,
            nomePublico(aluno),
            aluno.grr.value,
            tccAprovado,
            horas,
            requeridas,
        )
    }

    companion object {
        private fun nomePublico(aluno: Aluno): String {
            val social = aluno.nomeSocial?.trim().orEmpty()
            if (social.isNotEmpty()) {
                return social
            }
            val nome = aluno.nome.trim()
            return if (nome.isEmpty()) "Aluno" else nome
        }
    }
}
