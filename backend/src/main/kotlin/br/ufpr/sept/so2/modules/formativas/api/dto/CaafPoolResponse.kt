package br.ufpr.sept.so2.modules.formativas.api.dto

import br.ufpr.sept.so2.modules.formativas.application.CaafKpis
import br.ufpr.sept.so2.modules.formativas.application.CaafMembroCarga
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class CaafPoolResponse(
    val meuId: UUID,
    val kpis: CaafKpis,
    val membros: List<CaafMembroCarga>,
    val content: List<CaafFormativaResponse>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)

data class CaafFormativaResponse(
    val id: UUID,
    val idAluno: UUID,
    val alunoNome: String?,
    val idCurso: UUID?,
    val origem: String,
    val titulo: String,
    val cargaHoraria: Int,
    val estado: String,
    val createdAt: OffsetDateTime,
    val idResponsavel: UUID?,
    val responsavelRotulo: String?,
    val noPool: Boolean,
    val comigo: Boolean,
    val elegivelLote: Boolean,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)

data class AtribuirFormativaRequest(
    val formativaId: UUID?,
    val assigneeId: UUID?,
)

data class BatchAprovarFormativasRequest(
    val ids: List<UUID>?,
    val decisao: String?,
)
