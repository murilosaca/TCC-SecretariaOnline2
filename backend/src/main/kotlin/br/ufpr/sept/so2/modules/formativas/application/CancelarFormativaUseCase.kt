package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CancelarFormativaUseCase(
    private val alunoPorUsuarioPort: AlunoPorUsuarioPort,
    private val formativaRepository: FormativaRepository,
) {
    @Transactional
    fun execute(id: UUID, usuarioId: UUID): Formativa {
        val aluno = FormativaAcesso.exigirAlunoAtivo(alunoPorUsuarioPort, usuarioId)
        val formativa = formativaRepository.findById(id)
            ?: throw RecursoNaoEncontradoException("Formativa não encontrada.")
        FormativaAcesso.exigirDono(formativa, aluno.id)
        formativa.cancelar(OffsetDateTime.now())
        return formativaRepository.save(formativa)
    }
}
