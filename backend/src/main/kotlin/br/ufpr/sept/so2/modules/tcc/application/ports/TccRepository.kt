package br.ufpr.sept.so2.modules.tcc.application.ports

import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.modules.tcc.domain.TccEstado
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface TccRepository {
    fun save(tcc: Tcc): Tcc

    fun findById(id: UUID): Tcc?

    fun findByAluno(alunoId: UUID, estado: TccEstado?, pageable: Pageable): Page<Tcc>

    fun findParaRevisao(usuarioId: UUID, estado: TccEstado?, pageable: Pageable): Page<Tcc>

    fun existsAtivoByAluno(alunoId: UUID): Boolean
}
