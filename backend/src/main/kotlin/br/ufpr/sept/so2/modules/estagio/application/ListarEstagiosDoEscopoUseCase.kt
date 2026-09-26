package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListarEstagiosDoEscopoUseCase(
    private val estagioRepository: EstagioRepository,
    private val cursoEscopoPort: CursoEscopoPort,
) {
    @Transactional(readOnly = true)
    fun execute(atorId: UUID, situacao: String?, pageable: Pageable): Page<Estagio> {
        val pagina = EstagioPagina.de(pageable)
        val cursos = EstagioEscopo.cursos(cursoEscopoPort, atorId)
        if (cursos.isEmpty()) {
            return Page.empty(pagina)
        }
        return estagioRepository.findByCursos(cursos, EstagioPagina.situacao(situacao), pagina)
    }
}
