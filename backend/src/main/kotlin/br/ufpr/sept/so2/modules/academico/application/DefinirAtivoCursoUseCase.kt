package br.ufpr.sept.so2.modules.academico.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Desativa (ou reativa) o curso sem apagar aluno, disciplina ou histórico (RF-F5-004-a).
 * A exclusão continua no DELETE e responde 409 quando há vínculo.
 */
@Service
class DefinirAtivoCursoUseCase(
    private val cursoRepository: CursoRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(id: UUID, usuarioId: UUID, ativo: Boolean, ip: String?): Curso {
        if (id !in cursoEscopoPort.cursoIdsDoUsuario(usuarioId)) {
            throw RecursoNaoEncontradoException("Curso não encontrado.")
        }
        val curso = cursoRepository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Curso não encontrado.") }
        val antes = curso.ativo
        if (antes == ativo) {
            return curso
        }
        curso.atualizar(null, null, null, null, null, ativo)
        val salvo = cursoRepository.save(curso)
        val alteracoes = AuditMutacao.entre(
            mapOf("ativo" to antes.toString()),
            mapOf("ativo" to salvo.ativo.toString()),
        )
        val diff = objectMapper.writeValueAsString(
            mapOf(
                "entidade" to "curso",
                "cursoId" to id.toString(),
                "alteracoes" to alteracoes.map { mapOf("campo" to it.campo, "de" to it.de, "para" to it.para) },
            ),
        )
        auditLogPort.appendDiff(
            "curso.atualizado",
            usuarioId,
            objectMapper.writeValueAsString(mapOf("entidade" to "curso", "cursoId" to id.toString(), "ativo" to antes.toString())),
            diff,
            ip,
        )
        return salvo
    }
}
