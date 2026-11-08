package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
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
class ReenviarSolicitacaoUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
    private val workflowJsonParser: WorkflowJsonParser,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(solicitacaoId: UUID, atorId: UUID, ip: String?): Solicitacao {
        val solicitacao = solicitacaoRepository.findById(solicitacaoId)
            .orElseThrow { RecursoNaoEncontradoException("Solicitação não encontrada.") }
        if (!solicitacao.pertenceA(atorId)) {
            throw RecursoNaoEncontradoException("Solicitação não encontrada.")
        }
        val workflow = workflowJsonParser.parse(solicitacao.workflowSnapshot)
        if ("RESUBMIT" !in workflow.acoesDe(solicitacao.estado)) {
            throw ConflitoEstadoException("A solicitação não está em ajuste.")
        }
        val agora = OffsetDateTime.now()
        solicitacao.transicionar(Uuids.v7(), workflow, "RESUBMIT", atorId, null, agora)
        val salva = solicitacaoRepository.save(solicitacao)
        val evento = json(
            mapOf(
                "solicitacaoId" to salva.id.toString(),
                "protocolo" to salva.protocolo.valor,
                "estado" to salva.estado,
                "atorId" to atorId.toString(),
            ),
        )
        outboxPort.enqueue(TIPO, evento)
        auditLogPort.append(TIPO, atorId, evento, ip)
        return salva
    }

    private fun json(valor: Any?): String {
        try {
            return objectMapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o reenvio.")
        }
    }

    companion object {
        const val TIPO: String = "solicitacao.reenviada"
    }
}
