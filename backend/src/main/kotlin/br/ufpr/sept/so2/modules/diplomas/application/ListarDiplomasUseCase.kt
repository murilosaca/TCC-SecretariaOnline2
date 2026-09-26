package br.ufpr.sept.so2.modules.diplomas.application

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
    private val diplomaRepository: DiplomaRepository,
) {
    @Transactional(readOnly = true)
    fun execute(cursoId: UUID?, situacaoRaw: String?, pageable: Pageable): Page<Diploma> {
        val situacao = situacaoRaw?.takeIf { it.isNotBlank() }?.let { DiplomaSituacao.from(it) }
        val pagina = DiplomaPagina.de(pageable)
        if (cursoId == null) {
            return Page.empty(pagina)
        }
        return diplomaRepository.findByCurso(cursoId, situacao, pagina)
    }
}
