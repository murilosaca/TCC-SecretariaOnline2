package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.DisciplinaRepository
import br.ufpr.sept.so2.modules.academico.domain.Disciplina
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional
class DisciplinaApplicationService(
    private val disciplinaRepository: DisciplinaRepository,
    private val cursoRepository: CursoRepository,
) {
    @Transactional(readOnly = true)
    fun listar(idCurso: UUID?, pageable: Pageable): Page<Disciplina> =
        disciplinaRepository.findAll(idCurso, pageable)

    @Transactional(readOnly = true)
    fun buscarPorId(id: UUID): Disciplina =
        disciplinaRepository.findById(id)
            ?: throw RecursoNaoEncontradoException("Disciplina não encontrada: $id")

    fun criar(
        idCurso: UUID,
        codigo: String,
        nome: String,
        periodo: Int,
        cargaHorariaTotal: Int,
        creditos: Int,
    ): Disciplina {
        cursoRepository.findById(idCurso)
            .orElseThrow { RecursoNaoEncontradoException("Curso não encontrado: $idCurso") }
        val codigoNormalizado = codigo.trim().uppercase()
        if (disciplinaRepository.existsByCursoAndCodigo(idCurso, codigoNormalizado)) {
            throw ConflitoEstadoException("Já existe disciplina com o código $codigoNormalizado neste curso")
        }
        val agora = OffsetDateTime.now()
        val disciplina = Disciplina(
            id = Uuids.v7(),
            idCurso = idCurso,
            codigo = codigoNormalizado,
            nome = nome,
            periodo = periodo,
            cargaHorariaTotal = cargaHorariaTotal,
            creditos = creditos,
            ativa = true,
            createdAt = agora,
            updatedAt = agora,
        )
        return disciplinaRepository.save(disciplina)
    }

    fun atualizar(
        id: UUID,
        codigo: String?,
        nome: String?,
        periodo: Int?,
        carga: Int?,
        creditos: Int?,
        ativa: Boolean?,
    ): Disciplina {
        val disciplina = buscarPorId(id)
        disciplina.atualizar(codigo?.trim()?.uppercase(), nome, periodo, carga, creditos, ativa)
        return disciplinaRepository.save(disciplina)
    }

    fun excluir(id: UUID) {
        buscarPorId(id)
        disciplinaRepository.deleteById(id)
    }
}
