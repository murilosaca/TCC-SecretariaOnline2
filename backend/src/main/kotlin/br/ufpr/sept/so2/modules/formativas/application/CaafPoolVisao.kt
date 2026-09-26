package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import java.util.UUID

data class CaafKpis(
    val poolTotal: Long,
    val atribuidasAMim: Long,
    val prazoMedioDias: Double,
    val aprovadasNoPeriodo: Long,
)

data class CaafMembroCarga(
    val id: UUID,
    val rotulo: String?,
    val carga: Long,
    val cargaAlta: Boolean,
)

data class CaafPoolVisao(
    val meuId: UUID,
    val kpis: CaafKpis,
    val membros: List<CaafMembroCarga>,
    val itens: List<Formativa>,
)
