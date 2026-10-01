package br.ufpr.sept.so2.modules.diplomas.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ObterDiplomaUseCase(
    private val cursoEscopoPort: CursoEscopoPort,
    private val diplomaRepository: DiplomaRepository,
) {
    @Transactional(readOnly = true)
    fun execute(id: UUID, atorId: UUID): Diploma =
        DiplomaAcesso.exigirDiploma(DiplomaAcesso.cursos(cursoEscopoPort, atorId), diplomaRepository, id)
}
