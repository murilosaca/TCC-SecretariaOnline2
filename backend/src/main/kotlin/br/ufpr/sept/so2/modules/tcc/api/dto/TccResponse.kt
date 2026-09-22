package br.ufpr.sept.so2.modules.tcc.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class TccResponse(
    val id: UUID,
    val idAluno: UUID,
    val alunoNome: String?,
    val idCurso: UUID,
    val titulo: String,
    val situacao: String,
    val estado: String,
    val dataDefesa: LocalDate,
    val dataEntrega: LocalDate,
    val orientadorRotulo: String?,
    val papel: String?,
    val nomeArquivo: String?,
    val tamanho: Int?,
    val enviadoEm: OffsetDateTime?,
    val membros: List<MembroBancaResponse>,
    val avaliacoes: List<AvaliacaoTccResponse>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)

data class MembroBancaResponse(
    val id: UUID,
    val idUsuario: UUID,
    val rotulo: String?,
    val papel: String,
)

data class AvaliacaoTccResponse(
    val id: UUID,
    val idAutor: UUID,
    val autorRotulo: String?,
    val papel: String,
    val acao: String,
    val resultado: String,
    val nota: BigDecimal,
    val parecer: String,
    val createdAt: OffsetDateTime,
)
