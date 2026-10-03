package br.ufpr.sept.so2.modules.atendimentos.application.ports

import br.ufpr.sept.so2.modules.atendimentos.domain.Atendimento
import br.ufpr.sept.so2.modules.atendimentos.domain.AtendimentoEstado
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface AtendimentoRepository {
    fun save(atendimento: Atendimento): Atendimento

    fun findById(id: UUID): Atendimento?

    fun findByAluno(alunoId: UUID, estado: AtendimentoEstado?, pageable: Pageable): Page<Atendimento>
}
