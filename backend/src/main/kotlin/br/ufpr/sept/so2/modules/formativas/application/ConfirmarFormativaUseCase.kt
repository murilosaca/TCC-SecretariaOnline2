package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ConfirmarFormativaUseCase(
    private val alunoPorUsuarioPort: AlunoPorUsuarioPort,
    private val formativaRepository: FormativaRepository,
    private val outboxPort: OutboxPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(id: UUID, usuarioId: UUID): Formativa {
        val aluno = FormativaAcesso.exigirAlunoAtivo(alunoPorUsuarioPort, usuarioId)
        val formativa = formativaRepository.findById(id)
            ?: throw RecursoNaoEncontradoException("Formativa não encontrada.")
        FormativaAcesso.exigirDono(formativa, aluno.id)
        formativa.confirmar(OffsetDateTime.now())
        val salva = formativaRepository.save(formativa)
        outboxPort.enqueue("formativa.confirmada", payload(salva, usuarioId))
        return salva
    }

    private fun payload(formativa: Formativa, usuarioId: UUID): String =
        try {
            objectMapper.writeValueAsString(
                mapOf(
                    "formativaId" to formativa.id.toString(),
                    "alunoId" to formativa.idAluno.toString(),
                    "usuarioId" to usuarioId.toString(),
                    "estado" to formativa.estado.name,
                ),
            )
        } catch (_: JsonProcessingException) {
            "{\"formativaId\":\"${formativa.id}\"}"
        }
}
