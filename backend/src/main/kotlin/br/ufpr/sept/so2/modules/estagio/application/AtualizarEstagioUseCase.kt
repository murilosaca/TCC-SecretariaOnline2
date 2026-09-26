package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.EstagioSituacao
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AtualizarEstagioUseCase(
    private val estagioRepository: EstagioRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        atorId: UUID,
        estagioId: UUID,
        alunoId: UUID,
        empresa: String,
        supervisor: String,
        inicio: LocalDate,
        fim: LocalDate,
        ip: String?,
    ): Estagio {
        val cursos = EstagioEscopo.cursos(cursoEscopoPort, atorId)
        val estagio = EstagioEscopo.exigirEstagio(cursos, estagioRepository, estagioId)
        if (alunoId != estagio.idAluno && estagio.situacao != EstagioSituacao.CONCLUIDO) {
            throw DadoInvalidoException("O aluno do estágio não pode ser trocado.")
        }
        estagio.atualizarCadastro(empresa, supervisor, inicio, fim, OffsetDateTime.now())
        val salvo = estagioRepository.save(estagio)
        EstagioTrilha.registrar(outboxPort, auditLogPort, objectMapper, TIPO, atorId, salvo, ip)
        return salvo
    }

    companion object {
        const val TIPO = "estagio.atualizado"
    }
}
