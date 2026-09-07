package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaPorPresencaPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CriarFormativaPorPresencaUseCase(
    private val alunoPorUsuarioPort: AlunoPorUsuarioPort,
    private val formativaRepository: FormativaRepository,
    private val outboxPort: OutboxPort,
    private val objectMapper: ObjectMapper,
) : FormativaPorPresencaPort {

    override fun criarPendenteSeAusente(
        eventoId: UUID,
        usuarioId: UUID,
        titulo: String,
        cargaHoraria: Int,
    ) {
        val aluno = alunoPorUsuarioPort.resolver(usuarioId)
        if (aluno == null) {
            LOG.warn("Presença confirmada sem cadastro acadêmico; formativa não gerada.")
            return
        }
        if (aluno.egresso) {
            LOG.warn("Presença de egresso ignorada para formativa.")
            return
        }
        val existente = formativaRepository.findByEventoAndAluno(eventoId, aluno.id)
        val formativa = Formativa.viaPresencaOuExistente(
            existente,
            Uuids.v7(),
            aluno.id,
            eventoId,
            titulo,
            cargaHoraria,
            OffsetDateTime.now(),
        )
        if (existente != null && existente.id == formativa.id) {
            return
        }
        val salva = formativaRepository.save(formativa)
        outboxPort.enqueue("formativa.criada", payload(salva, usuarioId))
    }

    private fun payload(formativa: Formativa, usuarioId: UUID): String =
        try {
            objectMapper.writeValueAsString(
                mapOf(
                    "formativaId" to formativa.id.toString(),
                    "eventoId" to formativa.idEvento?.toString(),
                    "alunoId" to formativa.idAluno.toString(),
                    "usuarioId" to usuarioId.toString(),
                    "estado" to formativa.estado.name,
                ),
            )
        } catch (_: JsonProcessingException) {
            "{\"formativaId\":\"${formativa.id}\"}"
        }

    companion object {
        private val LOG = LoggerFactory.getLogger(CriarFormativaPorPresencaUseCase::class.java)
    }
}
