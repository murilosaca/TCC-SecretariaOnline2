package br.ufpr.sept.so2.modules.presenca.application.ports

import java.util.UUID

/** Sigla do curso para rotular o evento na lista da secretaria (F5.14). */
interface CursoSiglaPort {
    fun siglasPorId(ids: Collection<UUID>): Map<UUID, String>
}
