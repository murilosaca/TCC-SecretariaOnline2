package br.ufpr.sept.so2.modules.presenca.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.CursoSiglaPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class CursoSiglaAdapter(
    private val cursoRepository: CursoRepository,
) : CursoSiglaPort {

    @Transactional(readOnly = true)
    override fun siglasPorId(ids: Collection<UUID>): Map<UUID, String> =
        ids.distinct()
            .mapNotNull { id -> cursoRepository.findById(id).orElse(null) }
            .associate { curso -> curso.id to curso.sigla }
}
