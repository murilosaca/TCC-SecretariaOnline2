package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.application.ports.AutorEstagioPort
import br.ufpr.sept.so2.modules.estagio.application.ports.CoeMembroPort
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.Year
import java.util.UUID

@Service
class ObterPoolCoeUseCase(
    private val estagioRepository: EstagioRepository,
    private val coeMembroPort: CoeMembroPort,
    private val autorEstagioPort: AutorEstagioPort,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID): CoePoolVisao {
        val cursos = CoeAcesso.cursosDoMembro(coeMembroPort, usuarioId)
        if (cursos.isEmpty()) {
            return vazio(usuarioId)
        }
        val itens = estagioRepository.findPoolCoe(cursos, usuarioId)
        val membrosIds = coeMembroPort.membrosDosCursos(cursos)
        val cargas = estagioRepository.countCargaAtiva(cursos, membrosIds)
        val media = if (membrosIds.isEmpty()) 0.0 else cargas.values.sum().toDouble() / membrosIds.size
        val membros = membrosIds
            .map { id ->
                val carga = cargas[id] ?: 0L
                CoeMembroCarga(
                    id,
                    autorEstagioPort.rotulo(id),
                    carga,
                    membrosIds.size > 1 && carga > media,
                )
            }
            .sortedWith(compareBy<CoeMembroCarga> { it.rotulo ?: "" }.thenBy { it.id.toString() })
        return CoePoolVisao(
            usuarioId,
            CoeKpis(
                estagioRepository.countSemOrientador(cursos),
                estagioRepository.countAtribuidosAtivos(cursos, usuarioId),
                itens.count { it.documentoPendente() != null }.toLong(),
                estagioRepository.countConcluidosDesde(cursos, inicioDoAno()),
            ),
            membros,
            itens,
        )
    }

    private fun vazio(usuarioId: UUID) = CoePoolVisao(
        usuarioId,
        CoeKpis(0, 0, 0, 0),
        emptyList(),
        emptyList(),
    )

    private fun inicioDoAno(): OffsetDateTime =
        Year.now(ZoneOffset.UTC).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC)
}
