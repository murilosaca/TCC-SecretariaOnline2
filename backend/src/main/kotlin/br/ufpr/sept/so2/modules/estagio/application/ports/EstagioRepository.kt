package br.ufpr.sept.so2.modules.estagio.application.ports

import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.EstagioSituacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.UUID

interface EstagioRepository {
    fun save(estagio: Estagio): Estagio

    fun findById(id: UUID): Estagio?

    fun findByAluno(alunoId: UUID, situacao: EstagioSituacao?, pageable: Pageable): Page<Estagio>

    fun findParaRevisao(orientadorId: UUID, situacao: EstagioSituacao?, pageable: Pageable): Page<Estagio>

    fun findByCursos(cursoIds: Collection<UUID>, situacao: EstagioSituacao?, pageable: Pageable): Page<Estagio>

    fun findPoolCoe(cursoIds: Collection<UUID>, usuarioId: UUID): List<Estagio>

    fun countSemOrientador(cursoIds: Collection<UUID>): Long

    fun countAtribuidosAtivos(cursoIds: Collection<UUID>, orientadorId: UUID): Long

    fun countConcluidosDesde(cursoIds: Collection<UUID>, inicio: OffsetDateTime): Long

    fun countCargaAtiva(cursoIds: Collection<UUID>, orientadorIds: Collection<UUID>): Map<UUID, Long>

    fun existsByAluno(alunoId: UUID): Boolean

    fun existsSemOrientador(cursoId: UUID): Boolean
}
