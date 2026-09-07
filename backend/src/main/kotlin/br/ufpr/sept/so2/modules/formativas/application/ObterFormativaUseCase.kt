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
    fun execute(id: UUID, usuarioId: UUID): Formativa {
        val aluno = FormativaAcesso.exigirAlunoAtivo(alunoPorUsuarioPort, usuarioId)
        val formativa = formativaRepository.findById(id)
            ?: throw RecursoNaoEncontradoException("Formativa não encontrada.")
        FormativaAcesso.exigirDono(formativa, aluno.id)
        return formativa
    }
}
