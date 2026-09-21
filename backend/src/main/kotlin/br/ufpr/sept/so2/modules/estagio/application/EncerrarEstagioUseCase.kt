package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class EncerrarEstagioUseCase(
    private val estagioRepository: EstagioRepository,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(estagioId: UUID, revisorId: UUID, ip: String?): Estagio {
        val estagio = estagioRepository.findById(estagioId)
            ?: throw RecursoNaoEncontradoException("Estágio não encontrado.")
        EstagioAcesso.exigirOrientador(estagio, revisorId)
        estagio.encerrar(OffsetDateTime.now())
        val salvo = estagioRepository.save(estagio)
        val evento = EstagioJson.de(
            objectMapper,
            mapOf(
                "estagioId" to salvo.id.toString(),
                "alunoId" to salvo.idAluno.toString(),
                "situacao" to salvo.situacao.name,
                "revisorId" to revisorId.toString(),
            ),
        )
        outboxPort.enqueue(TIPO, evento)
        auditLogPort.append(TIPO, revisorId, evento, ip)
        return salvo
    }

    companion object {
        const val TIPO = "estagio.encerrado"
    }
}
