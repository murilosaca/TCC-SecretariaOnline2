package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoResumoPort
import br.ufpr.sept.so2.modules.formativas.application.ports.ComissaoMembroPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AtribuirFormativaCaafUseCase(
    private val formativaRepository: FormativaRepository,
    private val comissaoMembroPort: ComissaoMembroPort,
    private val alunoResumoPort: AlunoResumoPort,
    private val obterPoolCaafUseCase: ObterPoolCaafUseCase,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(atorId: UUID, formativaId: UUID, assigneeId: UUID, ip: String?): CaafPoolVisao {
        val cursos = CaafAcesso.cursosDoMembro(comissaoMembroPort, atorId)
        val formativa = formativaRepository.findById(formativaId)
            ?: throw RecursoNaoEncontradoException("Formativa não encontrada.")
        CaafAcesso.exigirNoEscopo(formativa, cursos, alunoResumoPort)
        if (!formativa.podeAtribuir(atorId)) {
            throw RecursoNaoEncontradoException("Formativa não encontrada.")
        }
        val cursoId = CaafAcesso.cursoDaFormativa(formativa, alunoResumoPort)
        if (!comissaoMembroPort.ehMembro(assigneeId, cursoId, CaafAcesso.TIPO)) {
            throw DadoInvalidoException("Destinatário não é membro da CAAF deste curso.")
        }
        val agora = OffsetDateTime.now()
        formativa.atribuirResponsavel(assigneeId, agora)
        val salva = formativaRepository.save(formativa)
        registrar(atorId, salva, assigneeId, ip)
        return obterPoolCaafUseCase.execute(atorId)
    }

    private fun registrar(atorId: UUID, formativa: Formativa, assigneeId: UUID, ip: String?) {
        val evento = toJson(
            mapOf(
                "formativaId" to formativa.id.toString(),
                "alunoId" to formativa.idAluno.toString(),
                "assigneeId" to assigneeId.toString(),
                "atorId" to atorId.toString(),
                "notificarResponsavel" to (assigneeId != atorId),
            ),
        )
        outboxPort.enqueue(TIPO, evento)
        auditLogPort.append(TIPO, atorId, evento, ip)
    }

    private fun toJson(valor: Any): String {
        try {
            return objectMapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o evento de atribuição.")
        }
    }

    companion object {
        const val TIPO = "formativas.assigned"
    }
}
