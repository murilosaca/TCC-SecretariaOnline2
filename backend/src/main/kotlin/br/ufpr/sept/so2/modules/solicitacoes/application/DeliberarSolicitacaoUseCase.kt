package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.iam.application.IamSettings
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.JtiBlacklistRepository
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.modules.solicitacoes.domain.WorkflowDefinicao
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
class DeliberarSolicitacaoUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
    private val workflowJsonParser: WorkflowJsonParser,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
    private val validarTokenDeliberacaoUseCase: ValidarTokenDeliberacaoUseCase,
    private val jtiBlacklistRepository: JtiBlacklistRepository,
    private val iamSettings: IamSettings,
    private val solicitacaoCursoEscopo: SolicitacaoCursoEscopo,
) {

    @Transactional
    fun execute(
        solicitacaoId: UUID,
        atorId: UUID,
        authorities: List<String>,
        acao: String?,
        parecer: String?,
        ip: String?,
        deepLinkToken: String? = null,
    ): Solicitacao {
        val tokenClaims = validarTokenDeliberacaoUseCase.execute(deepLinkToken, solicitacaoId, atorId)
        val solicitacao = solicitacaoRepository.findById(solicitacaoId)
            .orElseThrow { RecursoNaoEncontradoException("Solicitação não encontrada.") }
        if (authorities.contains(AUTHORITY_VIEW_CURSO) &&
            !solicitacaoCursoEscopo.solicitanteNoEscopo(atorId, solicitacao.solicitanteId)
        ) {
            throw RecursoNaoEncontradoException("Solicitação não encontrada.")
        }
        val workflow = workflowJsonParser.parse(solicitacao.workflowSnapshot)
        val acaoResolvida = workflow.resolverAcao(solicitacao.estado, acao)
        if (acaoResolvida !in WorkflowDefinicao.ACOES_DELIBERATIVAS) {
            throw DadoInvalidoException("Ação de deliberação inválida.")
        }
        val parecerLimpo = parecer?.trim().orEmpty()
        validarParecer(acaoResolvida, parecerLimpo)
        val agora = OffsetDateTime.now()
        solicitacao.transicionar(
            Uuids.v7(),
            workflow,
            acaoResolvida,
            atorId,
            parecerLimpo,
            agora,
        )
        val persistida = solicitacaoRepository.save(solicitacao)
        val evento = toJson(
            mapOf(
                "solicitacaoId" to persistida.id.toString(),
                "protocolo" to persistida.protocolo.valor,
                "acao" to acaoResolvida,
                "estado" to persistida.estado,
                "atorId" to atorId.toString(),
                "solicitanteId" to persistida.solicitanteId.toString(),
            ),
        )
        outboxPort.enqueue(tipoOutbox(acaoResolvida), evento)
        auditLogPort.append("solicitacao.deliberada", atorId, evento, ip)
        if (tokenClaims != null) {
            jtiBlacklistRepository.add(
                tokenClaims.jti,
                agora.plusSeconds(iamSettings.deliberationTtlSeconds),
            )
        }
        return persistida
    }

    private fun toJson(valor: Any?): String {
        try {
            return objectMapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o evento de deliberação.")
        }
    }

    companion object {
        const val AUTHORITY_VIEW_CURSO = "request.view_curso"
        private const val PARECER_INDEFER_MIN = 20

        private fun validarParecer(acao: String, parecer: String) {
            if (acao == "INDEFER" && parecer.length < PARECER_INDEFER_MIN) {
                throw DadoInvalidoException(
                    "Informe o parecer para indeferimento (mín. $PARECER_INDEFER_MIN caracteres).",
                )
            }
            if (parecer.isEmpty()) {
                throw DadoInvalidoException("Informe o parecer.")
            }
        }

        private fun tipoOutbox(acao: String): String =
            if (acao == "REQUEST_ADJUST" || acao == "REQUEST_ADJUSTMENT") {
                "solicitacao.ajuste_solicitado"
            } else {
                "solicitacao.deliberada"
            }
    }
}
