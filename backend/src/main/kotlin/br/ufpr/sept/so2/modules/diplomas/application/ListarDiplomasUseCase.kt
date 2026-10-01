package br.ufpr.sept.so2.modules.diplomas.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.modules.diplomas.domain.DiplomaSituacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListarDiplomasUseCase(
    private val cursoEscopoPort: CursoEscopoPort,
    private val diplomaRepository: DiplomaRepository,
) {
    @Transactional(readOnly = true)
    fun execute(cursoId: UUID, situacaoRaw: String?, pageable: Pageable, atorId: UUID): Page<Diploma> {
        DiplomaAcesso.exigirCursoNoEscopo(cursoId, DiplomaAcesso.cursos(cursoEscopoPort, atorId))
        val situacao = situacaoRaw?.takeIf { it.isNotBlank() }?.let { DiplomaSituacao.from(it) }
        return diplomaRepository.findByCurso(cursoId, situacao, DiplomaPagina.de(pageable))
    }
}
