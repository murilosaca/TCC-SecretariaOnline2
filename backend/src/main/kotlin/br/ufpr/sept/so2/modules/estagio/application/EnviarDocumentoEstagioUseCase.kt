package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.TipoDocumentoEstagio
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class EnviarDocumentoEstagioUseCase(
    private val alunoEstagioPort: AlunoEstagioPort,
    private val estagioRepository: EstagioRepository,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        estagioId: UUID,
        usuarioId: UUID,
        tipo: String?,
        nomeArquivo: String?,
        contentType: String?,
        bytes: ByteArray,
        ip: String?,
    ): Estagio {
        val aluno = EstagioAcesso.exigirAlunoAtivo(alunoEstagioPort, usuarioId)
        val estagio = estagioRepository.findById(estagioId)
            ?: throw RecursoNaoEncontradoException("Estágio não encontrado.")
        EstagioAcesso.exigirDono(estagio, aluno.id)
        val tipoDocumento = TipoDocumentoEstagio.from(tipo)
        val agora = OffsetDateTime.now()
        estagio.enviarDocumento(
            tipoDocumento,
            EstagioArquivo.nomeSeguro(nomeArquivo),
            contentType,
            bytes,
            agora,
        )
        val salvo = estagioRepository.save(estagio)
        val evento = EstagioJson.de(
            objectMapper,
            mapOf(
                "estagioId" to salvo.id.toString(),
                "alunoId" to salvo.idAluno.toString(),
                "orientadorId" to salvo.idOrientador.toString(),
                "tipo" to tipoDocumento.name,
                "estado" to "AGUARDANDO_PARECER",
            ),
        )
        outboxPort.enqueue(TIPO, evento)
        auditLogPort.append(TIPO, usuarioId, evento, ip)
        return salvo
    }

    companion object {
        const val TIPO = "estagio.documento_enviado"
    }
}
