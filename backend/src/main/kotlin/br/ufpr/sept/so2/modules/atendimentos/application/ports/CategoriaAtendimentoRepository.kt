package br.ufpr.sept.so2.modules.atendimentos.application.ports

import br.ufpr.sept.so2.modules.atendimentos.domain.CategoriaAtendimento
import java.util.UUID

interface CategoriaAtendimentoRepository {
    fun save(categoria: CategoriaAtendimento): CategoriaAtendimento

    fun findById(id: UUID): CategoriaAtendimento?

    fun findByNome(nome: String): CategoriaAtendimento?

    fun findAtivas(): List<CategoriaAtendimento>
}
