package br.ufpr.sept.so2.modules.comunicacao.application

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.UUID

data class RevisaoAtual(val assunto: String, val corpo: String)

data class TemplateResumo(val id: UUID, val nome: String, val assunto: String)

data class RevisaoItem(
    val versao: Int,
    val assunto: String,
    val corpo: String,
    val autorId: UUID,
    val criadoEm: OffsetDateTime,
    val status: String,
)

data class TemplateDetalhe(
    val id: UUID,
    val nome: String,
    val assunto: String,
    val corpo: String,
    val revisoes: List<RevisaoItem>,
)

interface TemplateComunicacaoRepository {
    fun existeNome(nome: String): Boolean

    fun criar(nome: String, assunto: String, corpo: String, autorId: UUID): TemplateDetalhe

    fun salvarRevisao(id: UUID, assunto: String, corpo: String, autorId: UUID): TemplateDetalhe

    fun buscar(id: UUID): TemplateDetalhe

    fun listar(pageable: Pageable): Page<TemplateResumo>

    fun revisaoAtualPorNome(nome: String): RevisaoAtual?
}
