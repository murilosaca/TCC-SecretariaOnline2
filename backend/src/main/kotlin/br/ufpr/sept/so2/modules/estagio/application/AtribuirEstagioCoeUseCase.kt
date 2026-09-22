package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.application.ports.CoeMembroPort
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AtribuirEstagioCoeUseCase(
    private val estagioRepository: EstagioRepository,
    private val coeMembroPort: CoeMembroPort,
    private val obterPoolCoeUseCase: ObterPoolCoeUseCase,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(atorId: UUID, estagioId: UUID, assigneeId: UUID, ip: String?): CoePoolVisao {
        val cursos = CoeAcesso.cursosDoMembro(coeMembroPort, atorId)
        val estagio = estagioRepository.findById(estagioId)
            ?: throw RecursoNaoEncontradoException("Estágio não encontrado.")
        CoeAcesso.exigirNoEscopo(estagio, cursos)
        if (!estagio.podeAtribuir(atorId)) {
            throw RecursoNaoEncontradoException("Estágio não encontrado.")
        }
        if (!coeMembroPort.ehMembro(assigneeId, estagio.idCurso)) {
            throw DadoInvalidoException("Destinatário não é membro do COE deste curso.")
        }
        val agora = OffsetDateTime.now()
        estagio.atribuirOrientador(assigneeId, agora)
        val salvo = estagioRepository.save(estagio)
        registrar(atorId, salvo, assigneeId, ip)
        return obterPoolCoeUseCase.execute(atorId)
    }

    private fun registrar(atorId: UUID, estagio: Estagio, assigneeId: UUID, ip: String?) {
        val evento = EstagioJson.de(
            objectMapper,
            mapOf(
                "estagioId" to estagio.id.toString(),
                "alunoId" to estagio.idAluno.toString(),
                "assigneeId" to assigneeId.toString(),
                "atorId" to atorId.toString(),
                "notificarAluno" to true,
                "notificarOrientador" to (assigneeId != atorId),
            ),
        )
        outboxPort.enqueue(TIPO, evento)
        auditLogPort.append(TIPO, atorId, evento, ip)
    }

    companion object {
        const val TIPO = "estagio.orientador_atribuido"
    }
}
