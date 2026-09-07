package br.ufpr.sept.so2.modules.academico.application.ports

import br.ufpr.sept.so2.modules.academico.domain.Curso
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

interface CursoRepository {
    fun save(curso: Curso): Curso

    fun findById(id: UUID): Optional<Curso>

    fun findAll(pageable: Pageable): Page<Curso>

    fun existsBySigla(sigla: String): Boolean

    fun existsByCodigo(codigo: String): Boolean

    fun findByCodigo(codigo: String): Optional<Curso>

    fun deleteById(id: UUID)
}
