package br.ufpr.sept.so2.modules.atendimentos.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.atendimentos.application.ports.AlunoAtendimentoPort
import br.ufpr.sept.so2.modules.atendimentos.application.ports.AtendimentoRepository
import br.ufpr.sept.so2.modules.atendimentos.application.ports.CategoriaAtendimentoRepository
import br.ufpr.sept.so2.modules.atendimentos.domain.AnexoAtendimento
import br.ufpr.sept.so2.modules.atendimentos.domain.Atendimento
import br.ufpr.sept.so2.modules.atendimentos.domain.CategoriaAtendimento
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class RegistrarAtendimentoUseCase(
    private val atendimentoRepository: AtendimentoRepository,
    private val categoriaRepository: CategoriaAtendimentoRepository,
    private val alunoAtendimentoPort: AlunoAtendimentoPort,
    private val cursoEscopoPort: CursoEscopoPort,
    private val objectStoragePort: ObjectStoragePort,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    /** A gravação, o Outbox e a trilha saem na mesma transação (RN-F5-007). */
    @Transactional
    fun execute(
        atorId: UUID,
        alunoId: UUID?,
        categoriaId: UUID?,
        assunto: String?,
        resposta: String?,
        anexo: ByteArray?,
        ip: String?,
        agora: OffsetDateTime = OffsetDateTime.now(),
    ): AtendimentoRegistrado {
        val cursos = AtendimentoAcesso.cursos(cursoEscopoPort, atorId)
        val aluno = AtendimentoAcesso.exigirAlunoNoEscopo(cursos, alunoAtendimentoPort, alunoId)
        val categoria = categoriaElegivel(categoriaId)
        exigirTexto(assunto, Atendimento.ASSUNTO_OBRIGATORIO)
        exigirTexto(resposta, Atendimento.RESPOSTA_OBRIGATORIA)
        val id = Uuids.v7()
        val storageKey = anexo?.let { gravarAnexo(id, it) }
        val salvo = atendimentoRepository.save(
            Atendimento.registrar(id, aluno.id, categoria.id, atorId, assunto, resposta, storageKey, agora),
        )
        outboxPort.enqueue(TIPO_OUTBOX, evento(salvo, categoria))
        auditLogPort.append(TIPO_AUDIT, atorId, evento(salvo, categoria), ip)
        return AtendimentoRegistrado(salvo, categoria)
    }

    private fun gravarAnexo(id: UUID, bytes: ByteArray): String {
        AnexoAtendimento.validar(bytes)
        val storageKey = "atendimentos/$id/anexo.pdf"
        objectStoragePort.putObject(storageKey, AnexoAtendimento.CONTENT_TYPE, bytes)
        return storageKey
    }

    private fun categoriaElegivel(categoriaId: UUID?): CategoriaAtendimento {
        val categoria = categoriaId?.let { categoriaRepository.findById(it) }
            ?: throw DadoInvalidoException(CATEGORIA_INVALIDA)
        if (!categoria.ativo) {
            throw DadoInvalidoException(CATEGORIA_INVALIDA)
        }
        return categoria
    }

    private fun exigirTexto(valor: String?, mensagem: String) {
        if (valor.isNullOrBlank()) {
            throw DadoInvalidoException(mensagem)
        }
    }

    private fun evento(atendimento: Atendimento, categoria: CategoriaAtendimento): String {
        try {
            return objectMapper.writeValueAsString(
                mapOf(
                    "atendimentoId" to atendimento.id.toString(),
                    "alunoId" to atendimento.idAluno.toString(),
                    "categoria" to categoria.nome,
                    "assunto" to atendimento.assunto,
                    "estado" to atendimento.estado.name,
                ),
            )
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível enfileirar o atendimento.")
        }
    }

    companion object {
        const val TIPO_OUTBOX = "atendimento.registrado"
        const val TIPO_AUDIT = "atendimento.registrado"
        const val CATEGORIA_INVALIDA = "Categoria de atendimento inválida."
    }
}

data class AtendimentoRegistrado(
    val atendimento: Atendimento,
    val categoria: CategoriaAtendimento,
)
