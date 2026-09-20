package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ObterFormativaUseCase(
    private val alunoPorUsuarioPort: AlunoPorUsuarioPort,
    private val formativaRepository: FormativaRepository,
) {
    @Transactional(readOnly = true)
    fun execute(id: UUID, usuarioId: UUID, authorities: List<String>): Formativa {
        val formativa = formativaRepository.findById(id)
            ?: throw RecursoNaoEncontradoException("Formativa não encontrada.")
        if (authorities.contains(AUTHORITY_REVIEW)) {
            return formativa
        }
        val aluno = FormativaAcesso.exigirAlunoAtivo(alunoPorUsuarioPort, usuarioId)
        FormativaAcesso.exigirDono(formativa, aluno.id)
        return formativa
    }

    companion object {
        const val AUTHORITY_REVIEW = "formative.review"
    }
}
