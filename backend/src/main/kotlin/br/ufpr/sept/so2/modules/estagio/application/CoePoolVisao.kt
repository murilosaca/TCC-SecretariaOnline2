package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import java.util.UUID

data class CoeKpis(
    val poolTotal: Long,
    val atribuidosAMim: Long,
    val documentosPendentes: Long,
    val concluidosNoPeriodo: Long,
)

data class CoeMembroCarga(
    val id: UUID,
    val rotulo: String?,
    val carga: Long,
    val cargaAlta: Boolean,
)

data class CoePoolVisao(
    val meuId: UUID,
    val kpis: CoeKpis,
    val membros: List<CoeMembroCarga>,
    val itens: List<Estagio>,
)
