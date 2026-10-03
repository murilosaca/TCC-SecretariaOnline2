package br.ufpr.sept.so2.modules.egresso.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.diplomas.domain.DiplomaSituacao
import br.ufpr.sept.so2.modules.egresso.application.ports.EgressoDiretorioPort
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Lista read-only de egressos da secretaria (F5.10, `alumni.list`).
 * A colação continua em `/diplomas` (fatia 16): aqui não há criação, PATCH de
 * entrega nem exportação assíncrona.
 */
@Service
class ListarEgressosUseCase(
    private val diplomaRepository: DiplomaRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val diretorioPort: EgressoDiretorioPort,
) {
    @Transactional(readOnly = true)
    fun execute(
        atorId: UUID,
        cursoId: UUID?,
        ano: Int?,
        situacao: String?,
        pageable: Pageable,
    ): Page<EgressoListado> {
        val cursos = EgressoEscopo.exigirCursos(cursoEscopoPort, atorId, cursoId)
        val pagina = diplomaRepository.findByCursos(
            cursos,
            ano,
            situacao?.takeIf { it.isNotBlank() }?.let { DiplomaSituacao.from(it) },
            paginar(pageable),
        )
        val alunos = diretorioPort.alunosPorId(pagina.content.map { it.idAluno })
        val siglas = diretorioPort.cursosPorId(pagina.content.map { it.idCurso })
        return pagina.map { diploma ->
            val aluno = alunos[diploma.idAluno]
            val curso = siglas[diploma.idCurso]
            EgressoListado(
                diploma.idAluno,
                aluno?.nome ?: SEM_NOME,
                aluno?.grr,
                diploma.idCurso,
                curso?.sigla ?: SEM_CURSO,
                curso?.nome,
                diploma.dataColacao,
                diploma.dataColacao.year,
                diploma.situacao.name,
                diploma.numero,
            )
        }
    }

    private fun paginar(pageable: Pageable): Pageable = PageRequest.of(
        pageable.pageNumber,
        pageable.pageSize.coerceAtMost(MAX_SIZE),
        Sort.by(Sort.Direction.DESC, "dataColacao"),
    )

    companion object {
        private const val MAX_SIZE = 100
        private const val SEM_NOME = "—"
        private const val SEM_CURSO = "—"
    }
}

internal object EgressoEscopo {
    const val MSG_FORA_ESCOPO = "Curso fora do escopo da sua secretaria."
    const val MSG_SEM_CURSO = "Nenhum curso vinculado à sua secretaria foi encontrado."

    /** Secretaria sem curso e curso fora do escopo recebem 403 antes de qualquer consulta. */
    fun exigirCursos(port: CursoEscopoPort, atorId: UUID, cursoId: UUID?): Set<UUID> {
        val vinculados = port.cursoIdsDoUsuario(atorId)
        if (vinculados.isEmpty()) {
            throw AcessoNegadoException(MSG_SEM_CURSO)
        }
        if (cursoId == null) {
            return vinculados
        }
        if (cursoId !in vinculados) {
            throw AcessoNegadoException(MSG_FORA_ESCOPO)
        }
        return setOf(cursoId)
    }
}

data class EgressoListado(
    val alunoId: UUID,
    val nome: String,
    val grr: String?,
    val cursoId: UUID,
    val cursoSigla: String,
    val cursoNome: String?,
    val dataColacao: OffsetDateTime,
    val anoColacao: Int,
    val situacaoDiploma: String,
    val numeroDiploma: String,
)
