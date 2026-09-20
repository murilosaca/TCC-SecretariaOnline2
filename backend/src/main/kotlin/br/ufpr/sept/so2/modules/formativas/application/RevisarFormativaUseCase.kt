package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoPorFormativaPort
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
class RevisarFormativaUseCase(
    private val formativaRepository: FormativaRepository,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
    private val certificadoPorFormativaPort: CertificadoPorFormativaPort,
) {

    @Transactional
    fun execute(
        formativaId: UUID,
        revisorId: UUID,
        acao: String?,
        parecer: String?,
        ip: String?,
    ): Formativa {
        val formativa = formativaRepository.findById(formativaId)
            ?: throw RecursoNaoEncontradoException("Formativa não encontrada.")
        val acaoResolvida = resolverAcao(acao)
        val parecerLimpo = Formativa.validarParecer(parecer, acaoResolvida == ACAO_INDEFERIR)
        val agora = OffsetDateTime.now()
        if (acaoResolvida == ACAO_APROVAR) {
            formativa.aprovar(parecerLimpo, revisorId, agora)
        } else {
            formativa.indeferir(parecerLimpo, revisorId, agora)
        }
        val persistida = formativaRepository.save(formativa)
        if (acaoResolvida == ACAO_APROVAR) {
            certificadoPorFormativaPort.emitirSeAusente(
                persistida.id,
                persistida.idAluno,
                persistida.idEvento,
                persistida.titulo,
                persistida.cargaHoraria,
                revisorId,
                ip,
            )
        }
        val evento = toJson(
            mapOf(
                "formativaId" to persistida.id.toString(),
                "alunoId" to persistida.idAluno.toString(),
                "acao" to acaoResolvida,
                "estado" to persistida.estado.name,
                "cargaHoraria" to persistida.cargaHoraria,
                "revisorId" to revisorId.toString(),
            ),
        )
        val tipo = tipoOutbox(acaoResolvida)
        outboxPort.enqueue(tipo, evento)
        auditLogPort.append(tipo, revisorId, evento, ip)
        return persistida
    }

    private fun toJson(valor: Any?): String {
        try {
            return objectMapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o evento de revisão.")
        }
    }

    companion object {
        const val ACAO_APROVAR = "APROVAR"
        const val ACAO_INDEFERIR = "INDEFERIR"

        private fun resolverAcao(acao: String?): String {
            val normalizada = acao?.trim()?.uppercase().orEmpty()
            if (normalizada != ACAO_APROVAR && normalizada != ACAO_INDEFERIR) {
                throw DadoInvalidoException("Ação de revisão inválida.")
            }
            return normalizada
        }

        private fun tipoOutbox(acao: String): String =
            if (acao == ACAO_APROVAR) "formativa.aprovada" else "formativa.indeferida"
    }
}
