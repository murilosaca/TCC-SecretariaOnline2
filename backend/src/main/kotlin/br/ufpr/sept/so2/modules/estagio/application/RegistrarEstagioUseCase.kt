package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.DocumentoEstagio
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.TipoDocumentoEstagio
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class RegistrarEstagioUseCase(
    private val estagioRepository: EstagioRepository,
    private val alunoEstagioPort: AlunoEstagioPort,
    private val cursoEscopoPort: CursoEscopoPort,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        atorId: UUID,
        alunoId: UUID,
        empresa: String,
        supervisor: String,
        inicio: LocalDate,
        fim: LocalDate,
        ip: String?,
    ): Estagio {
        val cursos = EstagioEscopo.cursos(cursoEscopoPort, atorId)
        val aluno = EstagioEscopo.exigirAluno(cursos, alunoEstagioPort, alunoId)
        val agora = OffsetDateTime.now()
        val estagio = Estagio.abrir(
            Uuids.v7(),
            aluno.id,
            aluno.idCurso,
            null,
            empresa,
            supervisor,
            inicio,
            fim,
            agora,
            listOf(
                DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.TCE),
                DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.RELATORIO_FINAL),
            ),
        )
        val salvo = estagioRepository.save(estagio)
        EstagioTrilha.registrar(outboxPort, auditLogPort, objectMapper, TIPO, atorId, salvo, ip)
        return salvo
    }

    companion object {
        const val TIPO = "estagio.registrado"
    }
}
