package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class EmitirParecerEstagioUseCase(
    private val estagioRepository: EstagioRepository,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        estagioId: UUID,
        documentoId: UUID,
        revisorId: UUID,
        acao: String?,
        parecer: String?,
        ip: String?,
    ): Estagio {
        val estagio = estagioRepository.findById(estagioId)
            ?: throw RecursoNaoEncontradoException("Estágio não encontrado.")
        EstagioAcesso.exigirOrientador(estagio, revisorId)
        val agora = OffsetDateTime.now()
        val registrado = estagio.emitirParecer(documentoId, acao, parecer, revisorId, Uuids.v7(), agora)
        val salvo = estagioRepository.save(estagio)
        val evento = EstagioJson.de(
            objectMapper,
            mapOf(
                "estagioId" to salvo.id.toString(),
                "documentoId" to documentoId.toString(),
                "alunoId" to salvo.idAluno.toString(),
                "acao" to registrado.acao,
                "revisorId" to revisorId.toString(),
            ),
        )
        outboxPort.enqueue(TIPO, evento)
        auditLogPort.append(TIPO, revisorId, evento, ip)
        return salvo
    }

    companion object {
        const val TIPO = "estagio.parecer_emitido"
    }
}
