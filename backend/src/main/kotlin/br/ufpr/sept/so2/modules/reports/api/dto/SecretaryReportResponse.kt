package br.ufpr.sept.so2.modules.reports.api.dto

import br.ufpr.sept.so2.modules.reports.domain.RelatorioSecretaria
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.ALWAYS)
data class SecretaryReportResponse(
    val filtros: FiltrosResponse,
    val solicitacoesPorTipo: List<PontoTipoResponse>,
    val solicitacoesPorEstado: List<PontoEstadoResponse>,
    val presencas: List<PontoPresencaResponse>,
    val horasFormativas: List<PontoHorasResponse>,
    val itensSolicitacao: List<ItemSolicitacaoResponse>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class FiltrosResponse(
        val periodo: String?,
        val periodoRotulo: String?,
        val curso: String?,
        val cursoId: UUID?,
        val cursoNome: String,
        val cursos: List<CursoEscopoResponse>,
    )

    data class CursoEscopoResponse(
        val id: UUID,
        val sigla: String,
        val nome: String,
    )

    data class PontoTipoResponse(
        val tipoCodigo: String,
        val tipoNome: String,
        val quantidade: Int,
    )

    data class PontoEstadoResponse(
        val estado: String,
        val quantidade: Int,
    )

    data class PontoPresencaResponse(
        val periodo: String,
        val confirmadas: Int,
        val registradas: Int,
    )

    data class PontoHorasResponse(
        val periodo: String,
        val horasValidadas: Int,
    )

    data class ItemSolicitacaoResponse(
        val id: UUID,
        val protocolo: String,
        val tipoCodigo: String,
        val tipoNome: String,
        val estado: String,
        val createdAt: OffsetDateTime,
        val cursoSigla: String,
    )

    companion object {
        fun from(relatorio: RelatorioSecretaria, links: Map<String, String>): SecretaryReportResponse =
            SecretaryReportResponse(
                filtros = FiltrosResponse(
                    periodo = relatorio.periodoCodigo,
                    periodoRotulo = relatorio.periodoRotulo,
                    curso = relatorio.cursoSigla,
                    cursoId = relatorio.cursoId,
                    cursoNome = relatorio.cursoNome,
                    cursos = relatorio.cursosEscopo.map {
                        CursoEscopoResponse(it.id, it.sigla, it.nome)
                    },
                ),
                solicitacoesPorTipo = relatorio.solicitacoesPorTipo.map {
                    PontoTipoResponse(it.tipoCodigo, it.tipoNome, it.quantidade)
                },
                solicitacoesPorEstado = relatorio.solicitacoesPorEstado.map {
                    PontoEstadoResponse(it.estado, it.quantidade)
                },
                presencas = relatorio.presencas.map {
                    PontoPresencaResponse(it.periodo, it.confirmadas, it.registradas)
                },
                horasFormativas = relatorio.horasFormativas.map {
                    PontoHorasResponse(it.periodo, it.horasValidadas)
                },
                itensSolicitacao = relatorio.itensSolicitacao.map {
                    ItemSolicitacaoResponse(
                        it.id,
                        it.protocolo,
                        it.tipoCodigo,
                        it.tipoNome,
                        it.estado,
                        it.createdAt,
                        it.cursoSigla,
                    )
                },
                links = links,
            )
    }
}
