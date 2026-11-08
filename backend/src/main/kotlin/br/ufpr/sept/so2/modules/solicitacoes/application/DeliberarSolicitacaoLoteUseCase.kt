package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.modules.solicitacoes.domain.WorkflowDefinicao
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Lote da fila de deliberação. Só corre quando o workflow publicado (gravado no
 * snapshot) tem `"batch": true`. AUTORIZACAO_IMAGEM continua no lote próprio
 * e não entra aqui.
 */
@Service
class DeliberarSolicitacaoLoteUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
    private val workflowJsonParser: WorkflowJsonParser,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(atorId: UUID, ids: List<UUID>, acao: String?, parecer: String?, ip: String?): List<Solicitacao> {
        val distintos = ids.distinct()
        if (distintos.isEmpty()) {
            throw DadoInvalidoException("Informe ao menos uma solicitação.")
        }
        val acaoResolvida = acao?.trim()?.uppercase().orEmpty()
        if (acaoResolvida !in WorkflowDefinicao.ACOES_DELIBERATIVAS) {
            throw DadoInvalidoException("Ação de deliberação inválida.")
        }
        val parecerLimpo = parecer?.trim().orEmpty()
        validarParecer(acaoResolvida, parecerLimpo)
        val travadas = solicitacaoRepository.findByIdsForUpdate(distintos)
        val porId = travadas.associateBy { it.id }
        if (distintos.any { it !in porId }) {
            throw DadoInvalidoException("Uma ou mais solicitações não foram encontradas.")
        }
        val itens = distintos.map { porId.getValue(it) }
        if (itens.any { it.tipoCodigo == TIPO_IMAGEM }) {
            throw DadoInvalidoException("Autorização de imagem usa o lote próprio da secretaria.")
        }
        if (itens.map { it.tipoCodigo }.distinct().size != 1) {
            throw DadoInvalidoException("O lote precisa ser de um único tipo de solicitação.")
        }
        val fluxos = itens.associate { item ->
            item.id to workflowJsonParser.parse(item.workflowSnapshot)
        }
        if (fluxos.values.any { !it.permiteLote }) {
            throw DadoInvalidoException("Este tipo de solicitação não permite deliberação em lote.")
        }
        fluxos.forEach { (id, fluxo) ->
            val item = porId.getValue(id)
            if (acaoResolvida !in fluxo.acoesDeliberativasDe(item.estado)) {
                throw ConflitoEstadoException("Transição inválida: ${item.estado} --$acaoResolvida-->")
            }
        }
        val agora = OffsetDateTime.now()
        return itens.map { item ->
            val fluxo = fluxos.getValue(item.id)
            item.transicionar(Uuids.v7(), fluxo, acaoResolvida, atorId, parecerLimpo, agora)
            val salva = solicitacaoRepository.save(item)
            val evento = objectMapper.writeValueAsString(
                mapOf(
                    "solicitacaoId" to salva.id.toString(),
                    "protocolo" to salva.protocolo.valor,
                    "acao" to acaoResolvida,
                    "estado" to salva.estado,
                    "atorId" to atorId.toString(),
                    "lote" to true,
                ),
            )
            val tipo = if (acaoResolvida == "REQUEST_ADJUST" || acaoResolvida == "REQUEST_ADJUSTMENT") {
                "solicitacao.ajuste_solicitado"
            } else {
                "solicitacao.deliberada"
            }
            outboxPort.enqueue(tipo, evento)
            auditLogPort.append("solicitacao.deliberada", atorId, evento, ip)
            salva
        }
    }

    companion object {
        const val TIPO_IMAGEM: String = "AUTORIZACAO_IMAGEM"
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
    }
}
