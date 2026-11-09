package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.DisciplinaRepository
import br.ufpr.sept.so2.modules.academico.application.ports.MatriculaRepository
import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.Disciplina
import br.ufpr.sept.so2.modules.academico.domain.Matricula
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.domain.exception.SemVagasException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

/**
 * F5.6-D05. Lote na mesma transação, com trava da disciplina.
 * Sem vaga: 422 no-vacancies e rollback. Sem outbox.
 */
@Service
class MatricularAlunoUseCase(
    private val alunoRepository: AlunoRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val disciplinaRepository: DisciplinaRepository,
    private val periodoLetivoRepository: PeriodoLetivoRepository,
    private val matriculaRepository: MatriculaRepository,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional(readOnly = true)
    fun opcoes(alunoId: UUID, operadorId: UUID): OpcoesMatricula {
        val aluno = alunoNoEscopo(alunoId, operadorId)
        val periodo = periodoLetivoRepository.findVigente(LocalDate.now()).orElse(null)
        val disciplinas = disciplinaRepository
            .findAll(aluno.idCurso, setOf(aluno.idCurso), PageRequest.of(0, 100))
            .content
            .filter { it.ativa }
        return OpcoesMatricula(
            periodo?.id,
            periodo?.let { "${it.ano}/${it.semestre}" },
            disciplinas.map { OpcaoDisciplina(it.id, it.codigo, it.nome, it.vagas) },
        )
    }

    @Transactional
    fun execute(alunoId: UUID, operadorId: UUID, disciplinaIds: List<UUID>, ip: String?): ResultadoMatricula {
        val aluno = alunoNoEscopo(alunoId, operadorId)
        val periodo = periodoLetivoRepository.findVigente(LocalDate.now())
            .orElseThrow { DadoInvalidoException("Não há período letivo vigente.") }
        val ids = disciplinaIds.distinct()
        if (ids.isEmpty()) {
            throw DadoInvalidoException("Selecione ao menos uma disciplina.")
        }
        val travadas = disciplinaRepository.lockByIds(ids).sortedBy { it.id }
        validar(travadas, ids, aluno, periodo.id)
        val agora = OffsetDateTime.now()
        travadas.forEach { disciplina ->
            disciplina.consumirVaga()
            disciplinaRepository.save(disciplina)
            matriculaRepository.save(Matricula(Uuids.v7(), aluno.id, disciplina.id, periodo.id, agora))
        }
        auditLogPort.append(
            "aluno.matriculado",
            operadorId,
            objectMapper.writeValueAsString(
                mapOf(
                    "alunoId" to aluno.id.toString(),
                    "periodoId" to periodo.id.toString(),
                    "disciplinaIds" to travadas.map { it.id.toString() },
                ),
            ),
            ip,
        )
        return ResultadoMatricula(periodo.id, travadas.map { it.id })
    }

    private fun validar(travadas: List<Disciplina>, ids: List<UUID>, aluno: Aluno, periodoId: UUID) {
        if (travadas.size != ids.size) {
            throw DadoInvalidoException(MSG_INDISPONIVEL)
        }
        val fora = travadas.any { it.idCurso != aluno.idCurso || !it.ativa }
        if (fora) {
            throw DadoInvalidoException(MSG_INDISPONIVEL)
        }
        val semVaga = travadas.firstOrNull { it.vagas <= 0 }
        if (semVaga != null) {
            throw SemVagasException(semVaga.id, semVaga.nome)
        }
        val repetida = travadas.firstOrNull { matriculaRepository.exists(aluno.id, it.id, periodoId) }
        if (repetida != null) {
            throw ConflitoEstadoException("O aluno já está matriculado em ${repetida.nome} neste período.")
        }
    }

    private fun alunoNoEscopo(alunoId: UUID, operadorId: UUID): Aluno {
        val aluno = alunoRepository.findById(alunoId)
            .orElseThrow { RecursoNaoEncontradoException("Aluno não encontrado.") }
        if (aluno.idCurso !in cursoEscopoPort.cursoIdsDoUsuario(operadorId)) {
            throw RecursoNaoEncontradoException("Aluno não encontrado.")
        }
        return aluno
    }

    data class ResultadoMatricula(
        val periodoId: UUID,
        val disciplinaIds: List<UUID>,
    )

    data class OpcoesMatricula(
        val periodoId: UUID?,
        val periodoRotulo: String?,
        val disciplinas: List<OpcaoDisciplina>,
    )

    data class OpcaoDisciplina(
        val id: UUID,
        val codigo: String,
        val nome: String,
        val vagas: Int,
    )

    companion object {
        private const val MSG_INDISPONIVEL = "Disciplina não disponível para matrícula neste curso."
    }
}
