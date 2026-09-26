package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.arquivos.domain.StorageKey
import br.ufpr.sept.so2.modules.tcc.application.ports.AlunoTccPort
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class EnviarVersaoFinalTccUseCase(
    private val alunoTccPort: AlunoTccPort,
    private val tccRepository: TccRepository,
    private val objectStoragePort: ObjectStoragePort,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        tccId: UUID,
        usuarioId: UUID,
        nomeArquivo: String?,
        contentType: String?,
        bytes: ByteArray,
        ip: String?,
    ): Tcc {
        val aluno = TccAcesso.exigirAlunoAtivo(alunoTccPort, usuarioId)
        val tcc = tccRepository.findById(tccId)
            ?: throw RecursoNaoEncontradoException("TCC não encontrado.")
        TccAcesso.exigirDono(tcc, aluno.id)
        val storageKey = StorageKey.tccVersaoFinal(tcc.id).value
        tcc.enviarVersaoFinal(TccArquivo.nomeSeguro(nomeArquivo), contentType, bytes, storageKey, OffsetDateTime.now())
        objectStoragePort.putObject(storageKey, "application/pdf", bytes)
        val salvo = tccRepository.save(tcc)
        val evento = TccJson.de(
            objectMapper,
            mapOf(
                "tccId" to salvo.id.toString(),
                "alunoId" to salvo.idAluno.toString(),
                "orientadorId" to salvo.idOrientador().toString(),
                "bancaIds" to salvo.membros.map { it.idUsuario.toString() },
                "estado" to salvo.estado.name,
                "storageKey" to storageKey,
            ),
        )
        outboxPort.enqueue(TIPO, evento)
        auditLogPort.append(TIPO, usuarioId, evento, ip)
        return salvo
    }

    companion object {
        const val TIPO = "tcc.submitted"
    }
}
