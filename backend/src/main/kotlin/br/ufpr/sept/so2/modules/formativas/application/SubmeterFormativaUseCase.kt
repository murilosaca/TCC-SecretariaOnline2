package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.application.ports.TipoAtividadeFormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.TipoAtividadeFormativa
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
class ListarTiposAtividadeFormativaUseCase(
    private val alunoPorUsuarioPort: AlunoPorUsuarioPort,
    private val tipoRepository: TipoAtividadeFormativaRepository,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID): List<TipoAtividadeFormativa> {
        val aluno = FormativaAcesso.exigirAlunoAtivo(alunoPorUsuarioPort, usuarioId)
        return tipoRepository.findAtivosDoCurso(aluno.cursoId)
    }
}

@Service
class SubmeterFormativaUseCase(
    private val alunoPorUsuarioPort: AlunoPorUsuarioPort,
    private val tipoRepository: TipoAtividadeFormativaRepository,
    private val formativaRepository: FormativaRepository,
    private val objectStoragePort: ObjectStoragePort,
    private val outboxPort: OutboxPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        usuarioId: UUID,
        tipoId: UUID?,
        cargaHoraria: Int?,
        bytes: ByteArray?,
        agora: OffsetDateTime = OffsetDateTime.now(),
    ): Formativa {
        val aluno = FormativaAcesso.exigirAlunoAtivo(alunoPorUsuarioPort, usuarioId)
        val tipo = tipoElegivel(tipoId, aluno.cursoId)
        val horas = cargaHoraria ?: throw DadoInvalidoException("Carga horária deve ser positiva.")
        if (horas <= 0) {
            throw DadoInvalidoException("Carga horária deve ser positiva.")
        }
        val conteudo = bytes ?: throw DadoInvalidoException(ComprovanteArquivo.MENSAGEM)
        val extensao = ComprovanteArquivo.extensao(conteudo)
        val id = Uuids.v7()
        val storageKey = "formativas/$id/comprovante.$extensao"
        objectStoragePort.putObject(storageKey, ComprovanteArquivo.contentType(extensao), conteudo)
        val salva = formativaRepository.save(
            Formativa.viaComprovante(id, aluno.id, tipo.id, tipo.nome, horas, storageKey, agora),
        )
        outboxPort.enqueue(TIPO_OUTBOX, evento(salva))
        return salva
    }

    private fun tipoElegivel(tipoId: UUID?, cursoId: UUID): TipoAtividadeFormativa {
        val tipo = tipoId?.let { tipoRepository.findById(it) }
            ?: throw DadoInvalidoException(FORA_DO_CURSO)
        if (!tipo.ativo || tipo.cursoId != cursoId) {
            throw DadoInvalidoException(FORA_DO_CURSO)
        }
        return tipo
    }

    private fun evento(formativa: Formativa): String {
        try {
            return objectMapper.writeValueAsString(
                mapOf(
                    "formativaId" to formativa.id.toString(),
                    "alunoId" to formativa.idAluno.toString(),
                    "tipoId" to formativa.idTipoAtividade.toString(),
                    "cargaHoraria" to formativa.cargaHoraria,
                ),
            )
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível enfileirar a submissão.")
        }
    }

    companion object {
        const val TIPO_OUTBOX = "formativas.submitted"
        const val FORA_DO_CURSO = "Tipo de atividade fora do curso."
    }
}
