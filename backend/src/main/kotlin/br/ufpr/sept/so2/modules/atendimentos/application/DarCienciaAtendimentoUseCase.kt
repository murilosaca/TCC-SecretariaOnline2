package br.ufpr.sept.so2.modules.atendimentos.application

import br.ufpr.sept.so2.modules.atendimentos.application.ports.AlunoAtendimentoPort
import br.ufpr.sept.so2.modules.atendimentos.application.ports.AtendimentoRepository
import br.ufpr.sept.so2.modules.atendimentos.application.ports.CategoriaAtendimentoRepository
import br.ufpr.sept.so2.modules.atendimentos.domain.Atendimento
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class DarCienciaAtendimentoUseCase(
    private val atendimentoRepository: AtendimentoRepository,
    private val categoriaRepository: CategoriaAtendimentoRepository,
    private val alunoAtendimentoPort: AlunoAtendimentoPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    /**
     * Transição e trilha na mesma transação: sem `audit_log` não há transição
     * e sem transição não há `audit_log` (RN-F1.20-03).
     */
    @Transactional
    fun execute(
        atendimentoId: UUID,
        usuarioId: UUID,
        ip: String?,
        agora: OffsetDateTime = OffsetDateTime.now(),
    ): AtendimentoItem {
        val cadastro = AtendimentoAcesso.exigirAlunoAutenticado(alunoAtendimentoPort, usuarioId)
        val atendimento = atendimentoRepository.findById(atendimentoId)
            ?: throw RecursoNaoEncontradoException(AtendimentoAcesso.NAO_ENCONTRADO)
        if (!atendimento.pertenceAoAluno(cadastro.id)) {
            throw RecursoNaoEncontradoException(AtendimentoAcesso.NAO_ENCONTRADO)
        }
        atendimento.darCiencia(ip, agora)
        val salvo = atendimentoRepository.save(atendimento)
        auditLogPort.append(TIPO_AUDIT, usuarioId, evento(salvo), ip)
        return AtendimentoItem(salvo, categoriaRepository.findById(salvo.idCategoria)?.nome)
    }

    private fun evento(atendimento: Atendimento): String {
        try {
            return objectMapper.writeValueAsString(
                mapOf(
                    "atendimentoId" to atendimento.id.toString(),
                    "alunoId" to atendimento.idAluno.toString(),
                    "cienciaEm" to atendimento.cienciaEm?.toString(),
                    "estado" to atendimento.estado.name,
                ),
            )
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível registrar a ciência.")
        }
    }

    companion object {
        const val TIPO_AUDIT = "atendimentos.acknowledged"
    }
}
