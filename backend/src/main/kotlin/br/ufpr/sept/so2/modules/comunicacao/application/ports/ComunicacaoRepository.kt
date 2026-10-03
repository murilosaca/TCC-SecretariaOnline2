package br.ufpr.sept.so2.modules.comunicacao.application.ports

import br.ufpr.sept.so2.modules.comunicacao.domain.Comunicacao
import java.util.UUID

interface ComunicacaoRepository {
    fun save(comunicacao: Comunicacao): Comunicacao

    fun findById(id: UUID): Comunicacao?

    fun findByAutor(autorId: UUID): List<Comunicacao>

    fun findByIds(ids: Collection<UUID>): List<Comunicacao>

    fun findByTitulo(titulo: String): Comunicacao?
}
