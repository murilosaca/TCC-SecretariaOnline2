package br.ufpr.sept.so2.modules.diplomas.application

import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ObterDiplomaUseCase(
    private val diplomaRepository: DiplomaRepository,
) {
    @Transactional(readOnly = true)
    fun execute(id: UUID): Diploma =
        diplomaRepository.findById(id)
            ?: throw RecursoNaoEncontradoException("Diploma não encontrado.")
}
