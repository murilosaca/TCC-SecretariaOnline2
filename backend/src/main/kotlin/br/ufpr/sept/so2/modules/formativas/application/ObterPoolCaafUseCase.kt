package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.formativas.application.ports.AutorFormativaPort
import br.ufpr.sept.so2.modules.formativas.application.ports.ComissaoMembroPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.Year
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ObterPoolCaafUseCase(
    private val formativaRepository: FormativaRepository,
    private val comissaoMembroPort: ComissaoMembroPort,
    private val autorFormativaPort: AutorFormativaPort,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID): CaafPoolVisao {
        val cursos = CaafAcesso.cursosDoMembro(comissaoMembroPort, usuarioId)
        if (cursos.isEmpty()) {
            return vazio(usuarioId)
        }
        val itens = formativaRepository.findPoolCaaf(cursos, usuarioId)
        val membrosIds = comissaoMembroPort.membrosDosCursos(cursos, CaafAcesso.TIPO)
        val cargas = formativaRepository.countCargaAguardando(cursos, membrosIds)
        val media = if (membrosIds.isEmpty()) 0.0 else cargas.values.sum().toDouble() / membrosIds.size
        val membros = membrosIds
            .map { id ->
                val carga = cargas[id] ?: 0L
                CaafMembroCarga(
                    id,
                    autorFormativaPort.rotulo(id),
                    carga,
                    membrosIds.size > 1 && carga > media,
                )
            }
            .sortedWith(compareBy<CaafMembroCarga> { it.rotulo ?: "" }.thenBy { it.id.toString() })
        val agora = OffsetDateTime.now(ZoneOffset.UTC)
        val prazoMedio = if (itens.isEmpty()) {
            0.0
        } else {
            itens.map { ChronoUnit.DAYS.between(it.createdAt, agora).coerceAtLeast(0).toDouble() }
                .average()
        }
        return CaafPoolVisao(
            usuarioId,
            CaafKpis(
                formativaRepository.countSemResponsavel(cursos),
                formativaRepository.countAtribuidasAguardando(cursos, usuarioId),
                prazoMedio,
                formativaRepository.countAprovadasDesde(cursos, inicioDoAno()),
            ),
            membros,
            itens,
        )
    }

    private fun vazio(usuarioId: UUID) = CaafPoolVisao(
        usuarioId,
        CaafKpis(0, 0, 0.0, 0),
        emptyList(),
        emptyList(),
    )

    private fun inicioDoAno(): OffsetDateTime =
        Year.now(ZoneOffset.UTC).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC)
}
