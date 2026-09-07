package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Curso
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
class CursoApplicationService(
    private val cursoRepository: CursoRepository,
) {
    @Transactional(readOnly = true)
    fun listar(pageable: Pageable): Page<Curso> = cursoRepository.findAll(pageable)

    @Transactional(readOnly = true)
    fun buscarPorId(id: UUID): Curso =
        cursoRepository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Curso não encontrado: $id") }

    fun criar(
        nome: String,
        sigla: String,
        codigo: String,
        idCoordenador: UUID?,
        horasFormativasMinimas: Int,
    ): Curso {
        if (cursoRepository.existsBySigla(sigla)) {
            throw ConflitoEstadoException("Já existe curso com a sigla $sigla")
        }
        if (cursoRepository.existsByCodigo(codigo)) {
            throw ConflitoEstadoException("Já existe curso com o código $codigo")
        }
        val agora = OffsetDateTime.now()
        val curso = Curso(
            id = Uuids.v7(),
            nome = nome,
            sigla = sigla.trim().uppercase(),
            codigo = codigo.trim().uppercase(),
            idCoordenador = idCoordenador,
            horasFormativasMinimas = horasFormativasMinimas,
            ativo = true,
            createdAt = agora,
            updatedAt = agora,
        )
        return cursoRepository.save(curso)
    }

    fun atualizar(
        id: UUID,
        nome: String?,
        sigla: String?,
        codigo: String?,
        idCoordenador: UUID?,
        horas: Int?,
        ativo: Boolean?,
    ): Curso {
        val curso = buscarPorId(id)
        curso.atualizar(nome, sigla?.trim()?.uppercase(), codigo?.trim()?.uppercase(), idCoordenador, horas, ativo)
        return cursoRepository.save(curso)
    }

    fun excluir(id: UUID) {
        buscarPorId(id)
        cursoRepository.deleteById(id)
    }
}
