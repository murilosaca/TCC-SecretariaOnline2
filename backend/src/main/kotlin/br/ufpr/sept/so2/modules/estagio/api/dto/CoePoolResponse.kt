package br.ufpr.sept.so2.modules.estagio.api.dto

import br.ufpr.sept.so2.modules.estagio.application.CoeKpis
import br.ufpr.sept.so2.modules.estagio.application.CoeMembroCarga
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.util.UUID

data class CoePoolResponse(
    val meuId: UUID,
    val kpis: CoeKpis,
    val membros: List<CoeMembroCarga>,
    val content: List<CoeEstagioResponse>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)

data class CoeEstagioResponse(
    val id: UUID,
    val idAluno: UUID,
    val alunoNome: String?,
    val idCurso: UUID,
    val idOrientador: UUID?,
    val orientadorRotulo: String?,
    val empresa: String,
    val inicio: LocalDate,
    val documentoPendente: String?,
    val noPool: Boolean,
    val comigo: Boolean,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)
