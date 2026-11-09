package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class EditarPayloadSolicitacaoUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
    private val workflowJsonParser: WorkflowJsonParser,
    private val formSchemaValidator: FormSchemaValidator,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(solicitacaoId: UUID, atorId: UUID, payload: Map<String, Any?>?, ip: String?): Solicitacao {
        val solicitacao = propria(solicitacaoId, atorId)
        val workflow = workflowJsonParser.parse(solicitacao.workflowSnapshot)
        if ("RESUBMIT" !in workflow.acoesDe(solicitacao.estado)) {
            throw ConflitoEstadoException("A solicitação não está em ajuste.")
        }
        formSchemaValidator.validar(solicitacao.formSchemaSnapshot, payload)
        val agora = OffsetDateTime.now()
        solicitacao.substituirPayload(json(payload), agora)
        val salva = solicitacaoRepository.save(solicitacao)
        auditLogPort.append(
            "solicitacao.payload_atualizado",
            atorId,
            json(mapOf("solicitacaoId" to salva.id.toString(), "protocolo" to salva.protocolo.valor)),
            ip,
        )
        return salva
    }

    private fun propria(solicitacaoId: UUID, atorId: UUID): Solicitacao {
        val solicitacao = solicitacaoRepository.findById(solicitacaoId)
            .orElseThrow { RecursoNaoEncontradoException("Solicitação não encontrada.") }
        if (!solicitacao.pertenceA(atorId)) {
            throw RecursoNaoEncontradoException("Solicitação não encontrada.")
        }
        return solicitacao
    }

    private fun json(valor: Any?): String {
        try {
            return objectMapper.writeValueAsString(valor ?: emptyMap<String, Any?>())
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o formulário.")
        }
    }
}
