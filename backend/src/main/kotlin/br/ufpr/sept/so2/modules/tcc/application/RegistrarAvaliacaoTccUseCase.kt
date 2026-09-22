package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Parecer individual. Não emite certificado: RF-F3-006 condiciona a emissão
 * a aluno elegível. F6.1 grava banca e limiar de horas, mas a consolidação
 * das avaliações e a colação (F5.11) continuam fora.
 */
@Service
class RegistrarAvaliacaoTccUseCase(
    private val tccRepository: TccRepository,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        tccId: UUID,
        revisorId: UUID,
        acao: String?,
        nota: BigDecimal?,
        parecer: String?,
        ip: String?,
    ): Tcc {
        val tcc = tccRepository.findById(tccId)
            ?: throw RecursoNaoEncontradoException("TCC não encontrado.")
        TccAcesso.exigirMembro(tcc, revisorId)
        val registrada = tcc.avaliar(acao, nota, parecer, revisorId, Uuids.v7(), OffsetDateTime.now())
        val salvo = tccRepository.save(tcc)
        val evento = TccJson.de(
            objectMapper,
            mapOf(
                "tccId" to salvo.id.toString(),
                "alunoId" to salvo.idAluno.toString(),
                "revisorId" to revisorId.toString(),
                "acao" to registrada.acao.name,
                "resultado" to registrada.resultado.name,
                "nota" to registrada.nota.toPlainString(),
            ),
        )
        outboxPort.enqueue(TIPO, evento)
        auditLogPort.append(TIPO, revisorId, evento, ip)
        return salvo
    }

    companion object {
        const val TIPO = "tcc.reviewed"
    }
}
