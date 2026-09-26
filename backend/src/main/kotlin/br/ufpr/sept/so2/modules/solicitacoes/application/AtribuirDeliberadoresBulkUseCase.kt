package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AtribuirDeliberadoresBulkUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
    private val solicitacaoCursoEscopo: SolicitacaoCursoEscopo,
    private val usuarioRepository: UsuarioRepository,
    private val workflowJsonParser: WorkflowJsonParser,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {

    @Transactional
    fun execute(
        atorId: UUID,
        authorities: List<String>,
        ids: List<UUID>,
        deliberadorId: UUID,
        ip: String?,
    ): List<Solicitacao> {
        if (ids.isEmpty()) {
            throw DadoInvalidoException("Informe ao menos uma solicitação.")
        }
        if (ids.size > MAX_LOTE) {
            throw DadoInvalidoException("Máximo de $MAX_LOTE solicitações por lote.")
        }
        val deliberador = usuarioRepository.findById(deliberadorId)
            .orElseThrow { DadoInvalidoException("Deliberador inválido.") }
        if (!deliberador.ativo || "request.deliberate" !in deliberador.authorities) {
            throw DadoInvalidoException("O usuário selecionado não pode deliberar.")
        }
        val agora = OffsetDateTime.now()
        val atualizadas = mutableListOf<Solicitacao>()
        for (id in ids.distinct()) {
            val solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow { RecursoNaoEncontradoException("Solicitação não encontrada.") }
            if (!solicitacaoCursoEscopo.solicitanteNoEscopo(atorId, solicitacao.solicitanteId)) {
                throw RecursoNaoEncontradoException("Solicitação não encontrada.")
            }
            if (!podeAtribuir(solicitacao, authorities)) {
                throw AcessoNegadoException("Atribuição em massa indisponível para esta solicitação.")
            }
            solicitacao.atribuirDeliberador(Uuids.v7(), deliberadorId, atorId, agora)
            val salva = solicitacaoRepository.save(solicitacao)
            val evento = toJson(
                mapOf(
                    "solicitacaoId" to salva.id.toString(),
                    "protocolo" to salva.protocolo.valor,
                    "por" to atorId.toString(),
                    "para" to deliberadorId.toString(),
                    "tipo" to Solicitacao.EVENTO_ATRIBUICAO,
                ),
            )
            auditLogPort.append("solicitacao.atribuida", atorId, evento, ip)
            atualizadas += salva
        }
        return atualizadas
    }

    private fun podeAtribuir(solicitacao: Solicitacao, authorities: List<String>): Boolean {
        if (!authorities.contains(AUTHORITY_TRIAGE) && !authorities.contains(AUTHORITY_DELIBERATE)) {
            return false
        }
        return try {
            workflowJsonParser.parse(solicitacao.workflowSnapshot)
                .acoesDeliberativasDe(solicitacao.estado)
                .isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    private fun toJson(valor: Any?): String {
        try {
            return objectMapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o evento de atribuição.")
        }
    }

    companion object {
        const val AUTHORITY_TRIAGE = "request.triage"
        const val AUTHORITY_DELIBERATE = "request.deliberate"
        private const val MAX_LOTE = 50
    }
}
