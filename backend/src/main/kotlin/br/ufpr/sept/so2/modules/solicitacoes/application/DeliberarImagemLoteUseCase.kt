package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitanteResumoPort
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.LoteConflitoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class DeliberarImagemLoteUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val solicitanteResumoPort: SolicitanteResumoPort,
    private val workflowJsonParser: WorkflowJsonParser,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        atorId: UUID,
        ids: List<UUID>,
        decisao: String,
        justificativa: String?,
        ip: String?,
    ): List<Solicitacao> {
        val distintos = ids.distinct()
        if (distintos.isEmpty()) {
            throw DadoInvalidoException("Informe ao menos uma solicitação.")
        }
        val acao = acaoDe(decisao)
        val cursos = cursoEscopoPort.cursoIdsDoUsuario(atorId)
        if (cursos.isEmpty()) {
            throw AcessoNegadoException("Nenhum curso vinculado à sua secretaria foi encontrado.")
        }
        val travadas = solicitacaoRepository.findByIdsForUpdate(distintos)
        val porId = travadas.associateBy { it.id }
        val divergentes = distintos.filter { id ->
            val item = porId[id]
            item == null || item.tipoCodigo != TIPO || item.estado != ESTADO_ABERTA
        }
        if (divergentes.isNotEmpty()) {
            throw LoteConflitoException(
                "Uma ou mais solicitações não estão abertas.",
                divergentes,
            )
        }
        val fora = travadas.filter { item ->
            val cursoId = solicitanteResumoPort.resumoDe(item.solicitanteId).cursoId
            cursoId == null || cursoId !in cursos
        }
        if (fora.isNotEmpty()) {
            throw AcessoNegadoException("Solicitação fora dos cursos vinculados à sua conta.")
        }
        val agora = OffsetDateTime.now()
        val parecer = justificativa?.trim()?.ifBlank { null }
        return distintos.map { id ->
            val item = porId.getValue(id)
            val workflow = workflowJsonParser.parse(item.workflowSnapshot)
            item.transicionar(Uuids.v7(), workflow, acao, atorId, parecer, agora)
            val salva = solicitacaoRepository.save(item)
            val evento = objectMapper.writeValueAsString(
                mapOf(
                    "solicitacaoId" to salva.id.toString(),
                    "protocolo" to salva.protocolo.valor,
                    "alunoId" to salva.solicitanteId.toString(),
                    "decisao" to salva.estado,
                    "justificativa" to parecer,
                ),
            )
            outboxPort.enqueue(TIPO_OUTBOX, evento)
            auditLogPort.append(TIPO_OUTBOX, atorId, evento, ip)
            salva
        }
    }

    private fun acaoDe(decisao: String): String =
        when (decisao.trim().uppercase()) {
            "DEFERIDA", "DEFER" -> "DEFER"
            "INDEFERIDA", "INDEFER" -> "INDEFER"
            else -> throw DadoInvalidoException("Decisão inválida. Use DEFERIDA ou INDEFERIDA.")
        }

    companion object {
        const val TIPO: String = "AUTORIZACAO_IMAGEM"
        const val ESTADO_ABERTA: String = "ABERTA"
        const val TIPO_OUTBOX: String = "solicitacao.imagem_deliberada"
    }
}
