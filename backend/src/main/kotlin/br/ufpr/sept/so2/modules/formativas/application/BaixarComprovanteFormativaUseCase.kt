package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.arquivos.application.PresignDownloadUseCase
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BaixarComprovanteFormativaUseCase(
    private val alunoPorUsuarioPort: AlunoPorUsuarioPort,
    private val formativaRepository: FormativaRepository,
    private val presignDownloadUseCase: PresignDownloadUseCase,
) {
    @Transactional(readOnly = true)
    fun execute(id: UUID, usuarioId: UUID, authorities: List<String>): String {
        val formativa = formativaRepository.findById(id)
            ?: throw RecursoNaoEncontradoException("Formativa não encontrada.")
        if (!authorities.contains(ObterFormativaUseCase.AUTHORITY_REVIEW)) {
            val aluno = FormativaAcesso.exigirAlunoAtivo(alunoPorUsuarioPort, usuarioId)
            FormativaAcesso.exigirDono(formativa, aluno.id)
        }
        val chave = formativa.storageKey
            ?: throw RecursoNaoEncontradoException("Formativa não encontrada.")
        return presignDownloadUseCase.execute(chave, "comprovante-${formativa.id}")
    }
}
