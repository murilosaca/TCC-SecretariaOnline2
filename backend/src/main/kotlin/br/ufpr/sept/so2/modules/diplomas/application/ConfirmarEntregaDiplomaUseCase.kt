package br.ufpr.sept.so2.modules.diplomas.application

import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.modules.diplomas.domain.MetodoEntregaDiploma
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ConfirmarEntregaDiplomaUseCase(
    private val diplomaRepository: DiplomaRepository,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        diplomaId: UUID,
        metodoRaw: String?,
        dataEntrega: OffsetDateTime?,
        atorId: UUID,
        ip: String?,
    ): Diploma {
        val diploma = diplomaRepository.findById(diplomaId)
            ?: throw RecursoNaoEncontradoException("Diploma não encontrado.")
        val metodo = MetodoEntregaDiploma.from(metodoRaw)
        val quando = dataEntrega ?: OffsetDateTime.now()
        val agora = OffsetDateTime.now()
        diploma.confirmarEntrega(metodo, quando, agora)
        val salvo = diplomaRepository.save(diploma)
        DiplomaTrilha.registrar(
            outboxPort,
            auditLogPort,
            objectMapper,
            TIPO,
            atorId,
            salvo,
            ip,
            mapOf(
                "metodoEntrega" to metodo.name,
                "dataEntrega" to quando.toString(),
            ),
        )
        return salvo
    }

    companion object {
        const val TIPO = "diploma.delivery_confirmed"
    }
}
