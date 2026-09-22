package br.ufpr.sept.so2.modules.academico.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.iam.application.ports.CoordenadorCursosPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CoordenadorCursosAdapter(
    private val cursoRepository: CursoRepository,
) : CoordenadorCursosPort {
    override fun ids(usuarioId: UUID): Set<UUID> = cursoRepository.findIdsByCoordenador(usuarioId)
}
