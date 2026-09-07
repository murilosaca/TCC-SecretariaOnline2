package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.ProtocoloSequenciaPort
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

@Service
class CriarSolicitacaoUseCase(
    private val tipoRepository: TipoSolicitacaoRepository,
    private val solicitacaoRepository: SolicitacaoRepository,
    private val protocoloSequenciaPort: ProtocoloSequenciaPort,
    private val formSchemaValidator: FormSchemaValidator,
    private val workflowJsonParser: WorkflowJsonParser,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {

    @Transactional
    fun execute(solicitanteId: UUID, tipoCodigo: String, payload: Map<String, Any?>?, ip: String?): Solicitacao {
        val tipo = tipoRepository.findByCodigo(tipoCodigo)
            .orElseThrow { RecursoNaoEncontradoException("Tipo de solicitação não encontrado.") }
        if (!tipo.isPublished()) {
            throw RecursoNaoEncontradoException("Tipo de solicitação não encontrado.")
        }
        formSchemaValidator.validar(tipo.formSchema, payload)
        val workflow = workflowJsonParser.parse(tipo.workflowJson)
        val agora = OffsetDateTime.now()
        val ano = agora.atZoneSameInstant(FUSO_SEPT).year
        val protocolo = protocoloSequenciaPort.proximo(ano)
        val solicitacao = Solicitacao.abrir(
            Uuids.v7(),
            Uuids.v7(),
            tipo,
            solicitanteId,
            protocolo,
            toJson(payload),
            workflow,
            agora,
        )
        val persistida = solicitacaoRepository.save(solicitacao)
        val evento = toJson(
            mapOf(
                "solicitacaoId" to persistida.id.toString(),
                "protocolo" to persistida.protocolo.valor,
                "tipoCodigo" to persistida.tipoCodigo,
                "solicitanteId" to solicitanteId.toString(),
            ),
        )
        outboxPort.enqueue("solicitacao.criada", evento)
        auditLogPort.append("solicitacao.criada", solicitanteId, evento, ip)
        return persistida
    }

    private fun toJson(valor: Any?): String {
        try {
            return objectMapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o formulário.")
        }
    }

    companion object {
        private val FUSO_SEPT: ZoneId = ZoneId.of("America/Sao_Paulo")
    }
}
