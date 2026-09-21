package br.ufpr.sept.so2.modules.estagio.application.ports

import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.EstagioSituacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface EstagioRepository {
    fun save(estagio: Estagio): Estagio

    fun findById(id: UUID): Estagio?

    fun findByAluno(alunoId: UUID, situacao: EstagioSituacao?, pageable: Pageable): Page<Estagio>

    fun findParaRevisao(orientadorId: UUID, situacao: EstagioSituacao?, pageable: Pageable): Page<Estagio>

    fun existsByAluno(alunoId: UUID): Boolean
}
