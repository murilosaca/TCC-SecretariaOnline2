package br.ufpr.sept.so2.modules.atendimentos.application

import br.ufpr.sept.so2.modules.arquivos.application.PresignDownloadUseCase
import br.ufpr.sept.so2.modules.atendimentos.application.ports.AlunoAtendimentoPort
import br.ufpr.sept.so2.modules.atendimentos.application.ports.AtendimentoRepository
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BaixarAnexoAtendimentoUseCase(
    private val atendimentoRepository: AtendimentoRepository,
    private val alunoAtendimentoPort: AlunoAtendimentoPort,
    private val presignDownloadUseCase: PresignDownloadUseCase,
) {
    @Transactional(readOnly = true)
    fun execute(atendimentoId: UUID, usuarioId: UUID): AnexoPresignado {
        val cadastro = AtendimentoAcesso.exigirAlunoAutenticado(alunoAtendimentoPort, usuarioId)
        val atendimento = atendimentoRepository.findById(atendimentoId)
            ?: throw RecursoNaoEncontradoException(AtendimentoAcesso.NAO_ENCONTRADO)
        if (!atendimento.pertenceAoAluno(cadastro.id)) {
            throw RecursoNaoEncontradoException(AtendimentoAcesso.NAO_ENCONTRADO)
        }
        val key = atendimento.storageKey
            ?: throw RecursoNaoEncontradoException("Atendimento sem anexo.")
        val nome = "atendimento-${atendimento.id}.pdf"
        return AnexoPresignado(nome, presignDownloadUseCase.execute(key, nome), presignDownloadUseCase.ttlSeconds())
    }
}

data class AnexoPresignado(
    val nomeArquivo: String,
    val downloadUrl: String,
    val expiresInSeconds: Long,
)
