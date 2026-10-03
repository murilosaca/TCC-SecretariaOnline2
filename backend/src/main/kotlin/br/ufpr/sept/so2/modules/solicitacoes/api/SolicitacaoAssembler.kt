package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.RequestTypeResponse
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.SolicitacaoEventoResponse
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.SolicitacaoResponse
import br.ufpr.sept.so2.modules.solicitacoes.application.WorkflowJsonParser
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitanteResumoPort
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.LinkedHashMap

@Component
class SolicitacaoAssembler(
    private val objectMapper: ObjectMapper,
    private val workflowJsonParser: WorkflowJsonParser,
    private val solicitanteResumoPort: SolicitanteResumoPort,
    private val objectStoragePort: ObjectStoragePort,
) {

    fun from(solicitacao: Solicitacao, authorities: List<String>, detalhe: Boolean): SolicitacaoResponse {
        val agora = OffsetDateTime.now()
        val vencido = solicitacao.prazoVencido(agora)
        val self = "/requests/" + solicitacao.id
        val links = LinkedHashMap<String, String>()
        links["self"] = self
        val acoes = acoesDoEstado(solicitacao)
        if (authorities.contains(AUTHORITY_DELIBERATE) && acoes.isNotEmpty()) {
            links["deliberar"] = self
            links["deliberate"] = "/solicitacoes/${solicitacao.id}/deliberar"
            if ("DEFER" in acoes) {
                links["deferir"] = "$self/transitions"
            }
            if ("INDEFER" in acoes) {
                links["indeferir"] = "$self/transitions"
            }
            if ("REQUEST_ADJUST" in acoes || "REQUEST_ADJUSTMENT" in acoes) {
                links["solicitar-ajustes"] = "$self/transitions"
            }
        }
        if (podeBulkAssign(authorities, acoes)) {
            links["bulk_assign"] = "/requests/bulk"
        }
        if (podeBulkDeliberar(solicitacao, authorities)) {
            links["bulk_deliberate"] = "/requests/bulk-deliberate"
        }
        val eventos = if (detalhe) {
            solicitacao.eventos.map(SolicitacaoEventoResponse::from)
        } else {
            emptyList()
        }
        val solicitante = solicitanteResumoPort.resumoDe(solicitacao.solicitanteId)
        val deliberadorNome = solicitacao.deliberadorId?.let { solicitanteResumoPort.nomeDe(it) }
        return SolicitacaoResponse(
            id = solicitacao.id,
            protocolo = solicitacao.protocolo.valor,
            tipoCodigo = solicitacao.tipoCodigo,
            tipoNome = solicitacao.tipoNome,
            tipoVersao = solicitacao.tipoVersao,
            estado = solicitacao.estado,
            payload = readMap(solicitacao.payloadJson),
            formSchema = if (detalhe) readMap(solicitacao.formSchemaSnapshot) else null,
            solicitanteNome = solicitante.nome,
            solicitanteGrr = solicitante.grr,
            cursoId = solicitante.cursoId,
            cursoNome = solicitante.cursoNome,
            cursoSigla = solicitante.cursoSigla,
            deliberadorId = solicitacao.deliberadorId,
            deliberadorNome = deliberadorNome,
            prazoEm = solicitacao.prazoEm,
            prazoVencido = vencido,
            sla = if (vencido) "ATRASADO" else "NO_PRAZO",
            slaStatus = slaStatus(solicitacao.prazoEm, agora),
            diasAtraso = diasAtraso(solicitacao.prazoEm, agora),
            createdAt = solicitacao.createdAt,
            updatedAt = solicitacao.updatedAt,
            thumbnailUrl = thumbnail(solicitacao, solicitante),
            eventos = eventos,
            links = links,
        )
    }

    fun from(tipo: TipoSolicitacao): RequestTypeResponse =
        RequestTypeResponse(
            tipo.id,
            tipo.codigo,
            tipo.nome,
            tipo.descricao,
            tipo.status,
            tipo.prazoDias,
            tipo.versao,
            readMap(tipo.formSchema),
            readMap(tipo.workflowJson),
            mapOf("self" to "/request-types/" + tipo.codigo),
        )

    private fun podeBulkDeliberar(solicitacao: Solicitacao, authorities: List<String>): Boolean =
        solicitacao.tipoCodigo == TIPO_IMAGEM &&
            solicitacao.estado == ESTADO_ABERTA_IMAGEM &&
            authorities.contains(AUTHORITY_IMAGEM)

    private fun thumbnail(
        solicitacao: Solicitacao,
        solicitante: br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitanteResumo,
    ): String? {
        if (solicitacao.tipoCodigo != TIPO_IMAGEM) {
            return null
        }
        val key = solicitante.fotoStorageKey ?: return null
        return try {
            objectStoragePort.presignGetUrl(key, Duration.ofMinutes(15))
        } catch (_: Exception) {
            null
        }
    }

    private fun podeBulkAssign(authorities: List<String>, acoes: Set<String>): Boolean {
        val podeTriar = authorities.contains(AUTHORITY_TRIAGE) ||
            (authorities.contains(AUTHORITY_VIEW_CURSO) && authorities.contains(AUTHORITY_DELIBERATE))
        return podeTriar && acoes.isNotEmpty()
    }

    private fun acoesDoEstado(solicitacao: Solicitacao): Set<String> =
        try {
            workflowJsonParser.parse(solicitacao.workflowSnapshot)
                .acoesDeliberativasDe(solicitacao.estado)
        } catch (_: Exception) {
            emptySet()
        }

    private fun readMap(json: String?): Map<String, Any?> {
        if (json.isNullOrBlank()) {
            return emptyMap()
        }
        return try {
            objectMapper.readValue(json, MAPA)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    companion object {
        const val AUTHORITY_DELIBERATE = "request.deliberate"
        const val AUTHORITY_TRIAGE = "request.triage"
        const val AUTHORITY_VIEW_CURSO = "request.view_curso"
        const val AUTHORITY_IMAGEM = "image_authorization.review"
        const val TIPO_IMAGEM = "AUTORIZACAO_IMAGEM"
        const val ESTADO_ABERTA_IMAGEM = "ABERTA"
        private val MAPA: TypeReference<Map<String, Any?>> = object : TypeReference<Map<String, Any?>>() {}

        fun slaStatus(prazoEm: OffsetDateTime?, agora: OffsetDateTime): String? {
            if (prazoEm == null) {
                return null
            }
            if (agora.isAfter(prazoEm)) {
                return "danger"
            }
            if (prazoEm.isBefore(agora.plusHours(24))) {
                return "warning"
            }
            return null
        }

        fun diasAtraso(prazoEm: OffsetDateTime?, agora: OffsetDateTime): Long? {
            if (prazoEm == null || !agora.isAfter(prazoEm)) {
                return null
            }
            return ChronoUnit.DAYS.between(prazoEm.toLocalDate(), agora.toLocalDate()).coerceAtLeast(0)
        }
    }
}
