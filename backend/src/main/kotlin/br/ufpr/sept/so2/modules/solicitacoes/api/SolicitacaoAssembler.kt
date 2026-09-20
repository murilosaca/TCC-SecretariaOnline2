package br.ufpr.sept.so2.modules.solicitacoes.api

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
import java.time.OffsetDateTime
import java.util.LinkedHashMap

@Component
class SolicitacaoAssembler(
    private val objectMapper: ObjectMapper,
    private val workflowJsonParser: WorkflowJsonParser,
    private val solicitanteResumoPort: SolicitanteResumoPort,
) {

    fun from(solicitacao: Solicitacao, authorities: List<String>, detalhe: Boolean): SolicitacaoResponse {
        val agora = OffsetDateTime.now()
        val vencido = solicitacao.prazoVencido(agora)
        val self = "/requests/" + solicitacao.id
        val links = LinkedHashMap<String, String>()
        links["self"] = self
        if (authorities.contains(AUTHORITY_DELIBERATE)) {
            val acoes = acoesDoEstado(solicitacao)
            if (acoes.isNotEmpty()) {
                links["deliberar"] = self
            }
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
        val eventos = if (detalhe) {
            solicitacao.eventos.map(SolicitacaoEventoResponse::from)
        } else {
            emptyList()
        }
        return SolicitacaoResponse(
            solicitacao.id,
            solicitacao.protocolo.valor,
            solicitacao.tipoCodigo,
            solicitacao.tipoNome,
            solicitacao.tipoVersao,
            solicitacao.estado,
            readMap(solicitacao.payloadJson),
            if (detalhe) readMap(solicitacao.formSchemaSnapshot) else null,
            solicitanteResumoPort.nomeDe(solicitacao.solicitanteId),
            solicitacao.prazoEm,
            vencido,
            if (vencido) "ATRASADO" else "NO_PRAZO",
            solicitacao.createdAt,
            solicitacao.updatedAt,
            eventos,
            links,
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
        private val MAPA: TypeReference<Map<String, Any?>> = object : TypeReference<Map<String, Any?>>() {}
    }
}
