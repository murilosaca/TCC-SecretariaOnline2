package br.ufpr.sept.so2.modules.formativas.application.ports

import br.ufpr.sept.so2.modules.formativas.domain.TipoAtividadeFormativa
import java.util.UUID

interface TipoAtividadeFormativaRepository {
    fun save(tipo: TipoAtividadeFormativa): TipoAtividadeFormativa

    fun findById(id: UUID): TipoAtividadeFormativa?

    fun findAtivosDoCurso(cursoId: UUID): List<TipoAtividadeFormativa>

    fun findByCursoENome(cursoId: UUID, nome: String): TipoAtividadeFormativa?
}
