package br.ufpr.sept.so2.modules.reports.infrastructure

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCursoEscopoPort
import br.ufpr.sept.so2.modules.reports.application.ports.RelatorioCursoEscopoPort.CursoEscopo
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RelatorioCursoEscopoAdapter(
    private val cursoRepository: CursoRepository,
    private val cursoSecretarioRepository: CursoSecretarioRepository,
) : RelatorioCursoEscopoPort {

    override fun idsCoordenados(usuarioId: UUID): Set<UUID> =
        cursoRepository.findIdsByCoordenador(usuarioId)

    override fun idsSecretariados(usuarioId: UUID): Set<UUID> =
        cursoSecretarioRepository.findCursoIdsByUsuarioId(usuarioId)

    override fun resolverCurso(cursoId: UUID): CursoEscopo? =
        cursoRepository.findById(cursoId).map { curso ->
            CursoEscopo(curso.id, curso.sigla, curso.nome, curso.idCoordenador)
        }.orElse(null)

    override fun localizarPorSigla(sigla: String, entre: Set<UUID>): CursoEscopo? {
        if (entre.isEmpty()) {
            return null
        }
        return entre.asSequence()
            .mapNotNull { resolverCurso(it) }
            .firstOrNull { it.sigla.equals(sigla, ignoreCase = true) }
    }

    override fun listarCursos(ids: Set<UUID>): List<CursoEscopo> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        return ids.mapNotNull { resolverCurso(it) }
            .sortedBy { it.sigla }
    }
}
